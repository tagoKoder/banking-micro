package com.sofka.tagoKoder.backend.account.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.sofka.tagoKoder.backend.account.model.Account;

import reactor.core.publisher.Flux;

public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {
    Flux<Account> findAllByClientId(Long clientId);
}
