package com.sofka.tagoKoder.backend.account.config;

import io.github.resilience4j.bulkhead.*;
import io.github.resilience4j.circuitbreaker.*;
import io.github.resilience4j.retry.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

  @Bean
  public CircuitBreaker clientServiceCircuitBreaker(CircuitBreakerRegistry registry) {
    return registry.circuitBreaker("clientService");
  }

  @Bean
  public Retry clientServiceRetry(RetryRegistry registry) {
    return registry.retry("clientService");
  }

  @Bean
  public Bulkhead clientServiceBulkhead(BulkheadRegistry registry) {
    return registry.bulkhead("clientService");
  }
}
