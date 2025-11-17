package com.sofka.tagoKoder.backend.account.domain.repository;


import com.sofka.tagoKoder.backend.account.domain.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository {
  Mono<Transaction> save(Transaction tx);
  Mono<Transaction> findById(Long id);
  Mono<Transaction> findLastByAccountId(Long accountId);
  Mono<Transaction> findLastByAccountIdUntil(Long accountId, LocalDateTime end);
  Mono<Long> countByAccountIdsAndDateBetween(List<Long> accountIds, LocalDateTime start, LocalDateTime end);
  Flux<Transaction> findPageByAccountIdsAndDateBetween(List<Long> accountIds,
                                                        LocalDateTime start, LocalDateTime end,
                                                        int page, int size, String sortBy, boolean asc);
}
