package com.sofka.tagoKoder.backend.account.integration.client;

import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.model.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ClientGatewayImpl implements ClientGateway {

  private final WebClient webClient;

  public ClientGatewayImpl(WebClient.Builder builder,
                           @Value("${client.service.base-url}") String baseUrl) {
    this.webClient = builder.baseUrl(baseUrl).build();
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
          // Si es 404, devolvemos vacío (cliente no existe)
          if (resp.statusCode() == HttpStatus.NOT_FOUND) return Mono.empty();
          return resp.createException().flatMap(Mono::error);
        })
        .bodyToMono(apiResponseType())
        .map(ApiResponse::getData)
        .switchIfEmpty(Mono.empty());
  }

  private static ParameterizedTypeReference<ApiResponse<ClientDto>> apiResponseType() {
    return new ParameterizedTypeReference<ApiResponse<ClientDto>>() {};
  }
}
