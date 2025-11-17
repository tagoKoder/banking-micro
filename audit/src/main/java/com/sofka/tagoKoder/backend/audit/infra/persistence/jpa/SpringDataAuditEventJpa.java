package com.sofka.tagoKoder.backend.audit.infra.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataAuditEventJpa extends JpaRepository<AuditEventEntity, UUID> {
  List<AuditEventEntity> findTop200ByAggregateTypeAndAggregateIdOrderByCreatedAtDesc(
      String aggregateType, String aggregateId);

  List<AuditEventEntity> findTop500ByTopicAndCreatedAtAfterOrderByCreatedAtDesc(
      String topic, Instant from);

  Optional<AuditEventEntity> findByTopicAndPartitionAndOffset(
      String topic, Integer partition, Long offset);
}
