package com.sofka.tagoKoder.backend.audit.infra.persistence.jpa;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events",
       uniqueConstraints = @UniqueConstraint(name="uq_topic_partition_offset",
          columnNames={"topic","partition","offset"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class AuditEventEntity {
  @Id private UUID id;
  @Column(nullable=false,length=200) private String topic;
  @Column(nullable=false) private Integer partition;
  @Column(name="\"offset\"",nullable=false) private Long offset;
  @Column(length=500) private String key;
  @Column(name="event_type",length=200) private String eventType;
  @Column(name="aggregate_type",length=200) private String aggregateType;
  @Column(name="aggregate_id",length=200) private String aggregateId;

  @Type(type="jsonb") @Column(columnDefinition="jsonb",nullable=false)
  private JsonNode headers;

  @Type(type="jsonb") @Column(columnDefinition="jsonb",nullable=false)
  private JsonNode payload;

  @Column(name="created_at",nullable=false,updatable=false)
  private Instant createdAt;
}
