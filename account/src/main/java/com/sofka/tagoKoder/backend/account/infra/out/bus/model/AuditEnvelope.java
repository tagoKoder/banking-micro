package com.sofka.tagoKoder.backend.account.infra.out.bus.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

@Value
@Builder
public class AuditEnvelope {
  String eventType;
  String aggregateType;
  String aggregateId;
  Instant occurredAt;
  Map<String, Object> data;
}
