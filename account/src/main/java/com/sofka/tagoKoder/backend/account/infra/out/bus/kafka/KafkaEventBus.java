package com.sofka.tagoKoder.backend.account.infra.out.bus.kafka;

import com.sofka.tagoKoder.backend.account.infra.out.bus.EventBus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class KafkaEventBus implements EventBus {

  private final KafkaTemplate<String, String> template;

  public KafkaEventBus(KafkaTemplate<String, String> template) {
    this.template = template;
  }

  @Override
  public Mono<Void> publish(String topic, String key, String payload) {
    return Mono.create(sink ->
        template.send(topic, key, payload).addCallback(
            result -> sink.success(),
            sink::error
        )
    );
  }
}
