package com.sofka.tagoKoder.backend.account.infra.out.http.client_service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.tagoKoder.backend.account.domain.out.ClientServicePort;
import com.sofka.tagoKoder.backend.account.domain.out.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.infra.rest.dto.ApiResponse;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Component
public class ClientServiceWebClient implements ClientServicePort {

  private final WebClient webClient;
  private final CircuitBreaker cb;
  private final Retry retry;
  private final Bulkhead bulkhead;
  private final ObjectMapper om; // <--- inyéctalo

  private static final ParameterizedTypeReference<ApiResponse<ClientDto>> TYPE =
      new ParameterizedTypeReference<>() {};

  public ClientServiceWebClient(
      WebClient.Builder builder,
      CircuitBreakerRegistry cbRegistry,
      RetryRegistry retryRegistry,
      BulkheadRegistry bhRegistry,
      ObjectMapper om,                                        // <---
      @Value("${client.service.base-url}") String baseUrl
  ) {
    this.webClient = builder
        .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
        .baseUrl(baseUrl)
        .build();
    this.cb = cbRegistry.circuitBreaker("clientService");
    this.retry = retryRegistry.retry("clientService");
    this.bulkhead = bhRegistry.bulkhead("clientService");
    this.om = om;                                            // <---
    log.info("[ClientService] baseUrl={}", baseUrl);
  }

  @Override
  public Mono<ClientDto> getById(Long id) {
    final var type = om.getTypeFactory()
        .constructParametricType(ApiResponse.class, ClientDto.class);

    return webClient.get()
        .uri("/{id}", id)
        .retrieve()
        // 404 => empty, otros 4xx/5xx => error
        .onStatus(HttpStatus::is4xxClientError, r ->
            r.statusCode() == HttpStatus.NOT_FOUND ? Mono.empty() : r.createException().flatMap(Mono::error))
        .bodyToMono(String.class)                                // lee cuerpo como String
        .flatMap(body ->                                         // parsea FUERA del event-loop
            Mono.fromCallable(() -> {
                  ApiResponse<ClientDto> wrap = om.readValue(body, type);
                  return wrap.getData();                         // puede ser null -> empty abajo
            })
            .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
        )
        .switchIfEmpty(Mono.empty())                             // 404 => empty
        .timeout(Duration.ofSeconds(2))                         // timeout más holgado
        .transformDeferred(CircuitBreakerOperator.of(cb))
        .transformDeferred(RetryOperator.of(retry))
        .transformDeferred(BulkheadOperator.of(bulkhead))
        .doOnSubscribe(s -> log.info("[ClientService.getById] GET /{}", id))
        .doOnNext(dto -> log.info("[ClientService.getById] OK dto={}", dto))
        .doOnError(e -> log.error("[ClientService.getById] {}", e.toString()));
        // IMPORTANTE: no uses onErrorResume -> empty aquí
  }

  @Override
  public Mono<Boolean> existsById(Long id) {
    return getById(id).map(dto -> true).defaultIfEmpty(false);
  }
}