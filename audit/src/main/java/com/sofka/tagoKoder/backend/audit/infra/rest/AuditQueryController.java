package com.sofka.tagoKoder.backend.audit.infra.rest;

import com.sofka.tagoKoder.backend.audit.domain.service.AuditEventService;
import com.sofka.tagoKoder.backend.audit.domain.model.AuditEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditQueryController {
  private final AuditEventService service;

  @GetMapping("/aggregate/{type}/{id}")
  public List<AuditEvent> byAggregate(@PathVariable String type, @PathVariable String id) {
    return service.findTop200ByAggregate(type, id);
  }

  @GetMapping("/topic/{topic}")
  public List<AuditEvent> byTopic(@PathVariable String topic, @RequestParam long sinceEpochMs) {
    return service.findTop500ByTopicSince(topic, Instant.ofEpochMilli(sinceEpochMs));
  }
}
