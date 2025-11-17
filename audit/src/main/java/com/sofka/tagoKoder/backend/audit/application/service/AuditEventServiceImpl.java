package com.sofka.tagoKoder.backend.audit.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.tagoKoder.backend.audit.domain.model.AuditEvent;
import com.sofka.tagoKoder.backend.audit.domain.repository.AuditEventRepository;
import com.sofka.tagoKoder.backend.audit.domain.service.AuditEventService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditEventServiceImpl implements AuditEventService {
  private final AuditEventRepository repo;
  private final ObjectMapper mapper;

  @Transactional
  @Override public void persist(ConsumerRecord<String,String> rec, Map<String,Object> headers) {
    if (repo.existsByTopicPartitionOffset(rec.topic(), rec.partition(), rec.offset())) return;

    JsonNode headersNode = mapper.valueToTree(headers);
    JsonNode payloadNode = safe(rec.value());

    AuditEvent e = AuditEvent.builder()
        .id(UUID.randomUUID())
        .topic(rec.topic())
        .partition(rec.partition())
        .offset(rec.offset())
        .key(rec.key())
        .eventType(text(payloadNode,"eventType"))
        .aggregateType(text(payloadNode,"aggregateType"))
        .aggregateId(text(payloadNode,"aggregateId"))
        .headers(headersNode)
        .payload(payloadNode)
        .createdAt(Instant.now())
        .build();

    repo.save(e);
  }

    @Override
  @Transactional(readOnly = true)
  public List<AuditEvent> findTop200ByAggregate(String aggregateType, String aggregateId) {
    return repo.findTop200ByAggregate(aggregateType, aggregateId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditEvent> findTop500ByTopicSince(String topic, Instant since) {
    return repo.findTop500ByTopicSince(topic, since);
  }

  private JsonNode safe(String json) {
    try { return mapper.readTree(json); }
    catch (Exception ex) { return mapper.createObjectNode().put("raw", json).put("parseError", ex.toString()); }
  }
  private String text(JsonNode n, String f){ return (n!=null && n.hasNonNull(f))? n.get(f).asText(): null; }
}
