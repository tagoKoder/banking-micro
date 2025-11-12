package com.sofka.tagoKoder.backend.account.service;

import com.sofka.tagoKoder.backend.account.exception.InsufficientBalanceException;
import com.sofka.tagoKoder.backend.account.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.integration.client.ClientGateway;
import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.mapper.BankStamentMapper;
import com.sofka.tagoKoder.backend.account.mapper.TransactionMapper;
import com.sofka.tagoKoder.backend.account.model.Transaction;
import com.sofka.tagoKoder.backend.account.model.dto.*;
import com.sofka.tagoKoder.backend.account.repository.TransactionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

  private final TransactionRepository transactionRepository;
  private final AccountService accountService;      // ¡ya reactivo!
  private final ClientGateway clientGateway;        // ¡ya reactivo (WebClient)!
  private final TransactionMapper transactionMapper;
  private final BankStamentMapper bankStatementMapper;
  private final R2dbcEntityTemplate template;

  public TransactionServiceImpl(TransactionRepository transactionRepository,
                                AccountService accountService,
                                ClientGateway clientGateway,
                                TransactionMapper transactionMapper,
                                BankStamentMapper bankStatementMapper,
                                R2dbcEntityTemplate template) {
    this.transactionRepository = transactionRepository;
    this.accountService = accountService;
    this.clientGateway = clientGateway;
    this.transactionMapper = transactionMapper;
    this.bankStatementMapper = bankStatementMapper;
    this.template = template;
  }

  // ---------- Paginado de transacciones (limit/offset con Template)
  @Override
  public Mono<PageResponse<TransactionDto>> getAll(Pageable pageable) {
    Sort sort = pageable.getSort().isUnsorted() ? Sort.by(Sort.Direction.ASC, "id") : pageable.getSort();
    Query q = Query.empty()
        .sort(sort)
        .limit(pageable.getPageSize())
        .offset(pageable.getOffset());

    Mono<Long> totalMono = template.count(Query.empty(), Transaction.class);
    Flux<TransactionDto> itemsFlux = template
        .select(q, Transaction.class)
        .map(transactionMapper::toTransactionDto);

    return itemsFlux.collectList()
        .zipWith(totalMono)
        .map(t -> {
          var content = t.getT1();
          long total = t.getT2();
          int totalPages = (int) Math.ceil(total / (double) pageable.getPageSize());
          boolean last = pageable.getOffset() + pageable.getPageSize() >= total;
          return new PageResponse<>(content,
              pageable.getPageNumber(),
              pageable.getPageSize(),
              total,
              totalPages,
              last);
        });
  }

  @Override
  public Mono<TransactionDto> getById(Long id) {
    return transactionRepository.findById(id)
        .switchIfEmpty(Mono.error(new NotFoundException("Transaction not found with id: " + id)))
        .map(transactionMapper::toTransactionDto);
  }

  // ---------- Crear transacción (cálculo de saldo)
  @Override
  public Mono<TransactionDto> create(TransactionDto transactionDto) {
    Long accId = transactionDto.getAccountId();

    // saldo base = último balance || initialAmount del account
    Mono<Double> baseBalance = transactionRepository
        .findFirstByAccountIdOrderByDateDesc(accId)
        .map(Transaction::getBalance)
        .switchIfEmpty(
            accountService.getById(accId).map(AccountDto::getInitialAmount)
        );

    return baseBalance.flatMap(balance -> {
      double newBalance = balance + transactionDto.getAmount();
      if (newBalance < 0) {
        return Mono.error(new InsufficientBalanceException("Saldo no disponible"));
      }
      transactionDto.setBalance(newBalance);
      transactionDto.setDate(new Date()); // si tu DTO usa Date; si usas LDT, pon LocalDateTime.now()

      Transaction toSave = transactionMapper.toModel(transactionDto);
      return transactionRepository.save(toSave)
          .map(transactionMapper::toTransactionDto);
    });
  }

  // ---------- Estado de cuenta paginado por cliente y rango de fechas
  @Override
  public Mono<PageResponse<BankStatementDto>> getStatementPage(
      Long clientId, LocalDate start, LocalDate end, Pageable pageable) {

    Mono<ClientDto> clientMono = clientGateway.getById(clientId)
        .switchIfEmpty(Mono.error(new NotFoundException("Client not found with id: " + clientId)));

    Mono<List<AccountDto>> accountsMono = accountService.getAllByClientId(clientId).collectList();

    return Mono.zip(clientMono, accountsMono)
        .flatMap(tuple -> {
          ClientDto client = tuple.getT1();
          List<AccountDto> accounts = tuple.getT2();

          if (accounts.isEmpty()) {
            return Mono.just(PageResponse.<BankStatementDto>builder()
                .content(List.of())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build());
          }

          List<Long> accountIds = accounts.stream().map(AccountDto::getId).collect(Collectors.toList());
          Map<Long, AccountDto> accMap = accounts.stream()
              .collect(Collectors.toMap(AccountDto::getId, a -> a));

          LocalDateTime startDt = start.atStartOfDay();
          LocalDateTime endDt = end.atTime(LocalTime.MAX);
          Sort sort = pageable.getSort().isUnsorted()
              ? Sort.by(Sort.Direction.ASC, "date")
              : pageable.getSort();

          Criteria criteria = Criteria.where("account_id").in(accountIds)
              .and(Criteria.where("date").between(startDt, endDt));

          Query pageQ = Query.query(criteria)
              .sort(sort)
              .limit(pageable.getPageSize())
              .offset(pageable.getOffset());

          Query countQ = Query.query(criteria);

          Flux<BankStatementDto> contentFlux = template.select(pageQ, Transaction.class)
              .map(tx -> {
                var txDto = transactionMapper.toTransactionDto(tx);
                var acc = accMap.get(tx.getAccountId());
                return bankStatementMapper.toDto(txDto, acc, client.getName());
              });

          Mono<Long> totalMono = template.count(countQ, Transaction.class);

          return contentFlux.collectList()
              .zipWith(totalMono)
              .map(t -> {
                var content = t.getT1();
                long total = t.getT2();
                int totalPages = (int) Math.ceil(total / (double) pageable.getPageSize());
                boolean last = pageable.getOffset() + pageable.getPageSize() >= total;
                return new PageResponse<>(content,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    total,
                    totalPages,
                    last);
              });
        });
  }

}
