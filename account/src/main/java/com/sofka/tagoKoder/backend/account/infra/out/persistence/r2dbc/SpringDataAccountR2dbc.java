package com.sofka.tagoKoder.backend.account.infra.out.persistence.r2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface SpringDataAccountR2dbc extends ReactiveCrudRepository<AccountEntity, Long> {
  Flux<AccountEntity> findAllByClientId(Long clientId);
}