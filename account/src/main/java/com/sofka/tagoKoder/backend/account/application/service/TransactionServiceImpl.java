package com.sofka.tagoKoder.backend.account.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.tagoKoder.backend.account.domain.event.DomainEvent;
import com.sofka.tagoKoder.backend.account.domain.model.Account;
import com.sofka.tagoKoder.backend.account.domain.model.Transaction;
import com.sofka.tagoKoder.backend.account.domain.out.ClientServicePort;
import com.sofka.tagoKoder.backend.account.domain.repository.AccountRepository;
import com.sofka.tagoKoder.backend.account.domain.repository.TransactionRepository;
import com.sofka.tagoKoder.backend.account.domain.service.TransactionService;
import com.sofka.tagoKoder.backend.account.infra.out.bus.EventBus;
import com.sofka.tagoKoder.backend.account.infra.rest.dto.PageSlice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

  private final TransactionRepository txRepo;    // dominio
  private final AccountRepository accountRepo;   // dominio
  private final ClientServicePort clientPort;    // puerto dominio (MS Client)
  private final EventBus eventBus;               // bus de eventos (Kafka/Redpanda)
  private final ObjectMapper json;               // serializer eventos

  private static final String TX_TOPIC = "transaction.events";

  // ---------- Helper: publicar evento sin bloquear ----------
  private void emitEventAsync(String eventType, Transaction tx) {
    publishEvent(eventType, tx)
        .timeout(Duration.ofMillis(300)) // límite corto para no colgar request
        .doOnError(e -> log.warn("[tx-event:{}] publish failed (ignored): {}", eventType, e.toString()))
        .subscribe();
  }

  private Mono<Void> publishEvent(String eventType, Transaction tx) {
    DomainEvent evt = DomainEvent.builder()
        .eventType(eventType)
        .aggregateType("Transaction")
        .aggregateId(String.valueOf(tx.getId()))
        .occurredAt(Instant.now())
        .data(Map.of(
            "id", tx.getId(),
            "accountId", tx.getAccountId(),
            "date", tx.getDate(),
            "type", tx.getType(),
            "amount", tx.getAmount(),
            "balance", tx.getBalance()
        ))
        .build();
    try {
      String key = String.valueOf(tx.getAccountId());
      String payload = json.writeValueAsString(evt);
      return eventBus.publish(TX_TOPIC, key, payload);
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  // ---------- Consultas ----------
  @Override
  public Mono<PageSlice<Transaction>> getAll(int page, int size, String sortBy, boolean asc) {
    Mono<Long> total = txRepo.countByAccountIdsAndDateBetween(List.of(), null, null).onErrorReturn(0L);
    Flux<Transaction> items = txRepo.findPageByAccountIdsAndDateBetween(
        List.of(), null, null, page, size, sortBy, asc);

    return items.collectList().zipWith(total)
        .map(t -> PageSlice.<Transaction>builder()
            .content(t.getT1())
            .page(page)
            .size(size)
            .totalElements(t.getT2())
            .totalPages((int) Math.ceil(t.getT2() / (double) size))
            .last(page >= (int) Math.ceil(t.getT2() / (double) size) - 1)
            .build());
  }

  @Override
  public Mono<Transaction> getById(Long id) {
    return txRepo.findById(id)
        .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found: " + id)));
  }

  // ---------- Crear transacción (no bloquear por Kafka) ----------
  @Override
  public Mono<Transaction> create(Transaction tx) {
    if (tx == null) return Mono.error(new IllegalArgumentException("Transaction is required"));
    if (tx.getAccountId() == null) return Mono.error(new IllegalArgumentException("accountId is required"));

    Long accId = tx.getAccountId();

    Mono<Account> accMono = accountRepo.findById(accId)
        .switchIfEmpty(Mono.error(new RuntimeException("Account not found: " + accId)));

    // Validar cliente del account
    Mono<Boolean> clientOk = accMono.flatMap(acc -> clientPort.existsById(acc.getClientId()))
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(new RuntimeException("Client not found for account: " + accId)));

    // Saldo base (último movimiento o initialAmount de la cuenta)
    Mono<Double> baseBalance = txRepo.findLastByAccountId(accId)
        .map(Transaction::getBalance)
        .switchIfEmpty(accMono.map(Account::getInitialAmount));

    return Mono.zip(clientOk, baseBalance, accMono)
        .flatMap(tuple -> {
          double newBalance = tuple.getT2() + tx.getAmount();
          if (newBalance < 0) return Mono.error(new RuntimeException("Saldo no disponible"));

          tx.setBalance(newBalance);
          if (tx.getDate() == null) tx.setDate(LocalDateTime.now());

          return txRepo.save(tx)
              .doOnNext(saved -> {
                log.info("[tx.create] saved id={} accountId={} amount={} balance={}",
                    saved.getId(), saved.getAccountId(), saved.getAmount(), saved.getBalance());
                emitEventAsync("TransactionCreated", saved); // ← fire & forget
              });
        });
  }

  @Override
  public Mono<PageSlice<Transaction>> getStatementPage(
      Long clientId, LocalDate start, LocalDate end, int page, int size, String sortBy, boolean asc) {

    LocalDateTime startDt = start.atStartOfDay();
    LocalDateTime endDt = end.atTime(LocalTime.MAX);

    Mono<Boolean> validClient = clientPort.existsById(clientId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(new RuntimeException("Client not found: " + clientId)));

    Mono<List<Account>> accountsMono = validClient
        .thenMany(accountRepo.findAllByClientId(clientId))
        .collectList();

    return accountsMono.flatMap(accounts -> {
      if (accounts.isEmpty()) {
        return Mono.just(PageSlice.<Transaction>builder()
            .content(List.of())
            .page(page)
            .size(size)
            .totalElements(0)
            .totalPages(0)
            .last(true)
            .build());
      }

      List<Long> accountIds = accounts.stream().map(Account::getId).collect(Collectors.toList());

      Mono<Long> total = txRepo.countByAccountIdsAndDateBetween(accountIds, startDt, endDt);
      Flux<Transaction> pageFlux = txRepo.findPageByAccountIdsAndDateBetween(
          accountIds, startDt, endDt, page, size, sortBy, asc);

      return pageFlux.collectList().zipWith(total)
          .map(t -> PageSlice.<Transaction>builder()
              .content(t.getT1())
              .page(page)
              .size(size)
              .totalElements(t.getT2())
              .totalPages((int) Math.ceil(t.getT2() / (double) size))
              .last(page >= (int) Math.ceil(t.getT2() / (double) size) - 1)
              .build());
    });
  }
}
