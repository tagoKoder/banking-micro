package com.sofka.tagoKoder.backend.account.repository;


import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sofka.tagoKoder.backend.account.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findFirstByAccountIdOrderByDateDesc(Long accountId);

    List<Transaction> findByAccountIdAndDateBetweenOrderByDate(
            Long accountId, Date startDate, Date endDate);
}
