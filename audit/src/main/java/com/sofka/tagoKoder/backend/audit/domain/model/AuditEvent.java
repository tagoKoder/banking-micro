package com.sofka.tagoKoder.backend.audit.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditEvent {
  private UUID id;
  private String topic;
  private Integer partition;
  private Long offset;
  private String key;
  private String eventType;
  private String aggregateType;
  private String aggregateId;
  private JsonNode headers;
  private JsonNode payload;
  private Instant createdAt;
}
