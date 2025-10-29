package com.sofka.tagoKoder.backend.account.repository;


import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sofka.tagoKoder.backend.account.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findFirstByAccountIdOrderByDateDesc(Long accountId);

        // Página de movimientos por cuenta y rango
    Page<Transaction> findByAccountIdAndDateBetween(
            Long accountId, Date start, Date end, Pageable pageable);
    Page<Transaction> findByAccountIdInAndDateBetween(
      List<Long> accountIds, Date start, Date end, Pageable pageable);

    // Último movimiento hasta 'end' (para calcular saldo final correcto)
    Optional<Transaction> findFirstByAccountIdAndDateLessThanEqualOrderByDateDesc(
            Long accountId, Date end);
}
