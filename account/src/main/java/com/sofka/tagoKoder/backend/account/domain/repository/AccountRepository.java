package com.sofka.tagoKoder.backend.account.domain.repository;

import com.sofka.tagoKoder.backend.account.domain.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepository {
  Mono<Account> save(Account account);
  Mono<Account> findById(Long id);
  Flux<Account> findAllByClientId(Long clientId);
  Mono<Long> countAll();
  Flux<Account> findPage(int page, int size, String sortBy, boolean desc);
}

