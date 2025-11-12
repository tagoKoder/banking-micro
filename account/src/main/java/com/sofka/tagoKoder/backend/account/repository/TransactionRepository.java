package com.sofka.tagoKoder.backend.account.repository;

import com.sofka.tagoKoder.backend.account.model.Transaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long> {

    // Último movimiento por cuenta
    Mono<Transaction> findFirstByAccountIdOrderByDateDesc(Long accountId);

    // Último movimiento hasta 'end' (para saldo final correcto)
    Mono<Transaction> findFirstByAccountIdAndDateLessThanEqualOrderByDateDesc(Long accountId, java.time.LocalDateTime end);
}
