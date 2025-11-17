package com.sofka.tagoKoder.backend.audit.domain.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.sofka.tagoKoder.backend.audit.domain.model.AuditEvent;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface AuditEventService {
  void persist(ConsumerRecord<String, String> rec, Map<String, Object> headers);
    List<AuditEvent> findTop200ByAggregate(String aggregateType, String aggregateId);

  List<AuditEvent> findTop500ByTopicSince(String topic, Instant since);
}
