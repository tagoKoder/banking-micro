// infra/persistence/r2dbc/TransactionRepositoryAdapter.java
package com.sofka.tagoKoder.backend.account.infra.out.persistence.r2dbc;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

import com.sofka.tagoKoder.backend.account.domain.model.Transaction;
import com.sofka.tagoKoder.backend.account.domain.repository.TransactionRepository;
import com.sofka.tagoKoder.backend.account.infra.out.persistence.r2dbc.mapper.TransactionR2dbcMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {

  private final SpringDataTransactionR2dbc repo;   // trabaja con TransactionEntity
  private final R2dbcEntityTemplate template;      // idem
  private final TransactionR2dbcMapper mapper;     // convierte Entity <-> Domain

  @Override
  public Mono<Transaction> save(Transaction tx) {
    return repo.save(mapper.toEntity(tx))
               .map(mapper::toDomain);
  }

  @Override
  public Mono<Transaction> findById(Long id) {
    return repo.findById(id).map(mapper::toDomain);
  }

  @Override
  public Mono<Transaction> findLastByAccountId(Long accountId) {
    return repo.findFirstByAccountIdOrderByDateDesc(accountId)
               .map(mapper::toDomain);
  }

  @Override
  public Mono<Transaction> findLastByAccountIdUntil(Long accountId, LocalDateTime end) {
    return repo.findFirstByAccountIdAndDateLessThanEqualOrderByDateDesc(accountId, end)
               .map(mapper::toDomain);
  }

  @Override
  public Mono<Long> countByAccountIdsAndDateBetween(List<Long> accountIds,
                                                    LocalDateTime start, LocalDateTime end) {
    Query q = Query.query(buildCriteria(accountIds, start, end));
    return template.count(q, TransactionEntity.class);
  }

  @Override
  public Flux<Transaction> findPageByAccountIdsAndDateBetween(List<Long> accountIds,
                                                              LocalDateTime start, LocalDateTime end,
                                                              int page, int size, String sortBy, boolean asc) {
    Sort sort = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC,
        (sortBy == null || sortBy.isBlank()) ? "date" : sortBy);

    Query q = Query.query(buildCriteria(accountIds, start, end))
                   .sort(sort)
                   .limit(size)
                   .offset((long) page * size);

    return template.select(q, TransactionEntity.class)
                   .map(mapper::toDomain);
  }

  // ---- helpers

  private Criteria buildCriteria(List<Long> accountIds, LocalDateTime start, LocalDateTime end) {
    Criteria c = Criteria.empty();

    if (accountIds != null && !accountIds.isEmpty()) {
      c = c.and(Criteria.where("account_id").in(accountIds));
    }
    if (start != null && end != null) {
      c = c.and(Criteria.where("date").between(start, end));
    } else if (start != null) {
      c = c.and(Criteria.where("date").greaterThanOrEquals(start));
    } else if (end != null) {
      c = c.and(Criteria.where("date").lessThanOrEquals(end));
    }
    return c;
  }
}
