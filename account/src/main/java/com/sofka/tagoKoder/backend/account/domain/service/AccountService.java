package com.sofka.tagoKoder.backend.account.domain.service;

import com.sofka.tagoKoder.backend.account.domain.model.Account;
import com.sofka.tagoKoder.backend.account.infra.rest.dto.PageSlice;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {
  Mono<PageSlice<Account>> getAll(int page, int size, String sortBy, String direction);
  Flux<Account> getAllByClientId(Long clientId);
  Mono<Account> getById(Long id);
  Mono<Account> create(Account account);
  Mono<Account> update(Long id, Account account);
  Mono<Account> partialUpdate(Long id, boolean active);
  Mono<Void> deleteById(Long id);
}