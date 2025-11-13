package com.sofka.tagoKoder.backend.account.integration.client;

import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.model.dto.ApiResponse;
import io.github.resilience4j.bulkhead.*;
import io.github.resilience4j.circuitbreaker.*;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class ClientGatewayImpl implements ClientGateway {

  private final WebClient webClient;
  private final CircuitBreaker cb;
  private final Retry retry;
  private final Bulkhead bulkhead;

  public ClientGatewayImpl(
      WebClient.Builder builder,
      CircuitBreaker clientServiceCircuitBreaker,
      Retry clientServiceRetry,
      Bulkhead clientServiceBulkhead,
      @Value("${client.service.base-url}") String baseUrl
  ) {
    this.webClient = builder.baseUrl(baseUrl).build();
    this.cb = clientServiceCircuitBreaker;
    this.retry = clientServiceRetry;
    this.bulkhead = clientServiceBulkhead;

    cb.getEventPublisher()
      .onStateTransition(e -> log.warn(
          "[CB clientService] {} -> {}",
          e.getStateTransition().getFromState(),
          e.getStateTransition().getToState()
      ));

  }

  @Override
  public Mono<ClientDto> getById(Long id) {
    return getByIdOrNull(id);
  }

  @Override
  public Mono<ClientDto> getByIdOrNull(Long id) {
    return webClient.get()
        .uri("/{id}", id)
        .retrieve()
        .onStatus(HttpStatus::is4xxClientError, resp -> {
          if (resp.statusCode() == HttpStatus.NOT_FOUND) return Mono.empty();
          return resp.createException().flatMap(Mono::error);
        })
        .bodyToMono(apiResponseType())
        .map(ApiResponse::getData)
        // Timeout a nivel Reactor (evita colgar la llamada)
        .timeout(Duration.ofSeconds(2))
        // Resilience4j (orden recomendado: CB -> Retry -> Bulkhead)
        .transformDeferred(CircuitBreakerOperator.of(cb))
        .transformDeferred(RetryOperator.of(retry))
        .transformDeferred(BulkheadOperator.of(bulkhead))
        // Fallback final (“declinet”): si después de CB/Retry/Bulkhead algo falla, no propaga error
        .onErrorResume(throwable -> {
          log.error("Fail to getByIdOrNull({}): {}. Applying fallback (Mono.empty).", id, throwable.toString());
          // Aquí podrías retornar Mono.just(cache.get(id)) si tuvieras caché local
          return Mono.empty();
        });
  }

  private static ParameterizedTypeReference<ApiResponse<ClientDto>> apiResponseType() {
    return new ParameterizedTypeReference<ApiResponse<ClientDto>>() {};
  }
}
