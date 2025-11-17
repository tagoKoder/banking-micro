// src/main/java/.../account/domain/service/TransactionService.java
package com.sofka.tagoKoder.backend.account.domain.service;

import com.sofka.tagoKoder.backend.account.domain.model.Transaction;
import com.sofka.tagoKoder.backend.account.infra.rest.dto.PageSlice;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface TransactionService {
  Mono<PageSlice<Transaction>> getAll(int page, int size, String sortBy, boolean asc);
  Mono<Transaction> getById(Long id);
  Mono<Transaction> create(Transaction tx);
  Mono<PageSlice<Transaction>> getStatementPage(
      Long clientId, LocalDate start, LocalDate end, int page, int size, String sortBy, boolean asc);
}
