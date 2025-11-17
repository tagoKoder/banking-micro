package com.sofka.tagoKoder.backend.account.domain.event;

import lombok.*;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent {

  /** Tipo del evento (p.ej., "AccountCreated", "AccountUpdated"). */
  private String eventType;

  /** Tipo del agregado (p.ej., "Account", "Transaction"). */
  private String aggregateType;

  /** Identificador del agregado (string para no acoplarse a tipos DB). */
  private String aggregateId;

  /** Momento en que ocurrió el evento (UTC). */
  private Instant occurredAt;

  /** Datos del evento (payload) en forma flexible. */
  private Map<String, Object> data;

  // --- Opcionales de trazabilidad (útiles si luego quieres APM/Tracing) ---
  /** Versión del esquema del evento (si versionas payloads). */
  @Builder.Default
  private String schemaVersion = "1.0";

  /** Versión del agregado (si mantienes versionado optimista). */
  private Long aggregateVersion;

  /** IDs para correlación/causación entre eventos y requests. */
  private String correlationId;
  private String causationId;

  /** Servicio/productor lógico que emite el evento (útil en auditoría). */
  private String producer;
}
