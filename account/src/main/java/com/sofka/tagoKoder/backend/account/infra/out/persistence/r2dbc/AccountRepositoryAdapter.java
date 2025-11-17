// infra/persistence/r2dbc/AccountRepositoryAdapter.java
package com.sofka.tagoKoder.backend.account.infra.out.persistence.r2dbc;

import com.sofka.tagoKoder.backend.account.domain.model.Account;
import com.sofka.tagoKoder.backend.account.domain.repository.AccountRepository;
import com.sofka.tagoKoder.backend.account.infra.out.persistence.r2dbc.mapper.AccountR2dbcMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

  private final SpringDataAccountR2dbc repo;
  private final R2dbcEntityTemplate template;
  private final AccountR2dbcMapper mapper;


  @Override
  public Mono<Account> save(Account account) {
    AccountEntity entity = mapper.toEntity(account);
    return repo.save(entity)
              .map(mapper::toDomain);
  }

  @Override
  public Mono<Account> findById(Long id) {
    return repo.findById(id)
              .map(mapper::toDomain);
  }

  @Override
  public Flux<Account> findAllByClientId(Long clientId) {
    return repo.findAllByClientId(clientId)
              .map(mapper::toDomain);
  }

  @Override
  public Mono<Long> countAll() {
    return template.count(Query.empty(), AccountEntity.class);
  }

  @Override
  public Flux<Account> findPage(int page, int size, String sortBy, boolean desc) {
    Sort sort = Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC,
        (sortBy == null || sortBy.isBlank()) ? "id" : sortBy);

    Query q = Query.empty()
                   .sort(sort)
                   .limit(size)
                   .offset((long) page * size);

    return template.select(q, AccountEntity.class)
                   .map(mapper::toDomain);
  }
}
