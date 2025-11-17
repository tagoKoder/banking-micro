package com.sofka.tagoKoder.backend.audit.domain.repository;

import com.sofka.tagoKoder.backend.audit.domain.model.AuditEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;

public interface AuditEventRepository {
  AuditEvent save(AuditEvent event);

  List<AuditEvent> findTop200ByAggregate(String aggregateType, String aggregateId);

  List<AuditEvent> findTop500ByTopicSince(String topic, Instant from);

  boolean existsByTopicPartitionOffset(String topic, int partition, long offset);

  AuditEvent findById(UUID id);

}
