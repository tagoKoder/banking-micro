package com.sofka.tagoKoder.backend.audit.infra.messaging;

import com.sofka.tagoKoder.backend.audit.domain.service.AuditEventService;
import com.sofka.tagoKoder.backend.audit.infra.config.KafkaConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

  private final AuditEventService service;

  @KafkaListener(
      topics = {"#{'${kafka.topics.account}'.split(',')}", "#{'${kafka.topics.client}'.split(',')}"},
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onEvent(ConsumerRecord<String,String> rec, Acknowledgment ack) {
    try {
      service.persist(rec, KafkaConfig.toHeaderMap(rec));
      ack.acknowledge();
    } catch (Exception e) {
      // el DefaultErrorHandler hará retries y DLT
      throw e;
    }
  }
}
