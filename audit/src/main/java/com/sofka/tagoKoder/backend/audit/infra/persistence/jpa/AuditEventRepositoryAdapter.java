package com.sofka.tagoKoder.backend.audit.infra.persistence.jpa;

import com.sofka.tagoKoder.backend.audit.domain.model.AuditEvent;
import com.sofka.tagoKoder.backend.audit.domain.repository.AuditEventRepository;
import com.sofka.tagoKoder.backend.audit.infra.rest.mapper.AuditEventMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AuditEventRepositoryAdapter implements AuditEventRepository {

  private final SpringDataAuditEventJpa jpa;
  private final AuditEventMapper mapper;

  @Override
  public AuditEvent save(AuditEvent event) {
    var saved = jpa.save(mapper.toEntity(event));
    return mapper.toDomain(saved);
  }

  @Override
  public List<AuditEvent> findTop200ByAggregate(String aggregateType, String aggregateId) {
    return mapper.toDomainList(
        jpa.findTop200ByAggregateTypeAndAggregateIdOrderByCreatedAtDesc(aggregateType, aggregateId)
    );
  }

  @Override
  public List<AuditEvent> findTop500ByTopicSince(String topic, Instant from) {
    return mapper.toDomainList(
        jpa.findTop500ByTopicAndCreatedAtAfterOrderByCreatedAtDesc(topic, from)
    );
  }

  @Override
  public boolean existsByTopicPartitionOffset(String topic, int partition, long offset) {
    return jpa.findByTopicAndPartitionAndOffset(topic, partition, offset).isPresent();
  }

  @Override
  public AuditEvent findById(UUID id) {
    return jpa.findById(id).map(mapper::toDomain).orElse(null);
  }
}
