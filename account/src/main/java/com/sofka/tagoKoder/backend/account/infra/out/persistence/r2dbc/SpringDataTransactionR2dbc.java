package com.sofka.tagoKoder.backend.account.infra.out.persistence.r2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface SpringDataTransactionR2dbc extends ReactiveCrudRepository<TransactionEntity, Long> {
  Mono<TransactionEntity> findFirstByAccountIdOrderByDateDesc(Long accountId);
  Mono<TransactionEntity> findFirstByAccountIdAndDateLessThanEqualOrderByDateDesc(Long accountId, LocalDateTime end);
}