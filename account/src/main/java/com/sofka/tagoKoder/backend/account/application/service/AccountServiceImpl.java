package com.sofka.tagoKoder.backend.account.application.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.tagoKoder.backend.account.domain.event.DomainEvent;
import com.sofka.tagoKoder.backend.account.domain.model.Account;
import com.sofka.tagoKoder.backend.account.domain.out.ClientServicePort;
import com.sofka.tagoKoder.backend.account.domain.repository.AccountRepository;
import com.sofka.tagoKoder.backend.account.domain.service.AccountService;
import com.sofka.tagoKoder.backend.account.infra.out.bus.EventBus;
import com.sofka.tagoKoder.backend.account.infra.rest.dto.PageSlice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepo;  // Trabaja con dominio
  private final ClientServicePort clientPort;   // Para validar cliente remoto
  private final EventBus eventBus;              // Publicar eventos
  private final ObjectMapper json;              // Serializar eventos

  

  private static final String ACCOUNT_TOPIC = "account.events";

  @Override
  public Mono<PageSlice<Account>> getAll(int page, int size, String sortBy, String direction) {
    boolean desc = "desc".equalsIgnoreCase(direction);
    Mono<Long> totalMono = accountRepo.countAll();
    Flux<Account> items = accountRepo.findPage(page, size, sortBy, desc);

    return items.collectList().zipWith(totalMono)
        .map(t -> {
          var content = t.getT1();
          long total = t.getT2();
          int totalPages = (int) Math.ceil(total / (double) size);
          boolean last = page >= (totalPages - 1);
          return PageSlice.<Account>builder()
              .content(content)
              .page(page)
              .size(size)
              .totalElements(total)
              .totalPages(totalPages)
              .last(last)
              .build();
        });
  }

  @Override
  public Flux<Account> getAllByClientId(Long clientId) {
    return accountRepo.findAllByClientId(clientId);
  }

  @Override
  public Mono<Account> getById(Long id) {
    return accountRepo.findById(id)
        .switchIfEmpty(Mono.error(new RuntimeException("Account not found: " + id)));
  }

  @Override
  public Mono<Account> create(Account account) {
    if (account == null) return Mono.error(new IllegalArgumentException("Account is required"));
    if (account.getClientId() == null) return Mono.error(new IllegalArgumentException("clientId is required"));
    if (account.getType() == null) return Mono.error(new IllegalArgumentException("type is required"));
    if (account.getNumber() != null) account.setNumber(account.getNumber().trim());

    return clientPort.getById(account.getClientId())
        .doOnSubscribe(s -> log.info("[create] checking clientId={}", account.getClientId()))
        .switchIfEmpty(Mono.error(new RuntimeException("Client not found: " + account.getClientId())))
        .flatMap(__ -> accountRepo.save(account))
        .doOnNext(saved -> {
          log.info("[create] saved-ok id={}", saved.getId());
          emitEventAsync("AccountCreated", saved);
        });
  }


  @Override
  public Mono<Account> update(Long id, Account account) {
    return accountRepo.findById(id)
        .switchIfEmpty(Mono.error(new RuntimeException("Account not found: " + id)))
        .flatMap(existing -> {
          existing.setType(account.getType());
          existing.setActive(account.isActive());
          return accountRepo.save(existing);
        })
        .doOnNext(saved -> emitEventAsync("AccountUpdated", saved))
        ;
  }


  @Override
  public Mono<Account> partialUpdate(Long id, boolean active) {
    return accountRepo.findById(id)
        .switchIfEmpty(Mono.error(new RuntimeException("Account not found: " + id)))
        .flatMap(acc -> {
          acc.setActive(active);
          return accountRepo.save(acc);
        })
        .doOnNext(saved -> emitEventAsync("AccountPatched", saved))
        ;
  }


  @Override
  public Mono<Void> deleteById(Long id) {
    return accountRepo.findById(id)
        .switchIfEmpty(Mono.error(new RuntimeException("Account not found: " + id)))
        .flatMap(acc -> {
          acc.setActive(false);
          return accountRepo.save(acc)
              .doOnNext(saved -> emitEventAsync("AccountDeactivated", saved)); // ← fire & forget
        })
        .then();
  }


  private void emitEventAsync(String type, Account acc) {
  publishEvent(type, acc)
      .timeout(Duration.ofMillis(300))        // límite duro (ajusta a gusto)
      .doOnError(e -> log.warn("[event:{}] publish failed (ignored): {}", type, e.toString()))
      .subscribe();                            // no bloquea la respuesta
}


private Mono<Void> publishEvent(String eventType, Account acc) {
  Map<String, Object> data = new java.util.HashMap<>();
  data.put("id", acc.getId());
  data.put("clientId", acc.getClientId());
  data.put("type", acc.getType());
  data.put("active", acc.isActive());
  if (acc.getNumber() != null)        data.put("number", acc.getNumber());

  DomainEvent evt = DomainEvent.builder()
      .eventType(eventType)
      .aggregateType("Account")
      .aggregateId(String.valueOf(acc.getId()))
      .occurredAt(java.time.Instant.now())
      .data(data)
      .build();

  try {
    String key = String.valueOf(acc.getId());
    String payload = json.writeValueAsString(evt);
    return eventBus.publish(ACCOUNT_TOPIC, key, payload);
  } catch (Exception e) {
    return reactor.core.publisher.Mono.error(e);
  }
}

}
