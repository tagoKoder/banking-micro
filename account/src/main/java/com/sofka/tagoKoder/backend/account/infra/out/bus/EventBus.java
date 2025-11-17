package com.sofka.tagoKoder.backend.account.infra.out.bus;

import reactor.core.publisher.Mono;

public interface EventBus {
  Mono<Void> publish(String topic, String key, String payload);
}
