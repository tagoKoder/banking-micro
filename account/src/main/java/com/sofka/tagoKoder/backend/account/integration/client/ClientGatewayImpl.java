package com.sofka.tagoKoder.backend.account.integration.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.model.dto.ApiResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
public class ClientGatewayImpl implements ClientGateway {

  private final RestTemplate rest;
  private final String clientServiceBaseUrl;
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClientGatewayImpl.class);

  public ClientGatewayImpl(RestTemplate rest,
                           @Value("${client.service.base-url}") String clientUrl) {
    this.rest = rest;
    this.clientServiceBaseUrl = clientUrl;
  }

  @Override
  public Optional<ClientDto> getById(Long id) {
    return Optional.ofNullable(getByIdOrNull(id));
  }

  @Override
  @Cacheable(value = "clients", key = "#id", unless = "#result == null")
  @CircuitBreaker(name = "clientService", fallbackMethod = "getByIdOrNullFallback")
  @Retry(name = "clientService")
  public ClientDto getByIdOrNull(Long id) {
    String url = UriComponentsBuilder.fromHttpUrl(clientServiceBaseUrl)
        .path("/{id}").buildAndExpand(id).toUriString();

    try {
      ResponseEntity<ApiResponse<ClientDto>> res = rest.exchange(
          url, HttpMethod.GET, null,
          new org.springframework.core.ParameterizedTypeReference<ApiResponse<ClientDto>>() {});
      ApiResponse<ClientDto> body = res.getBody();
      if (body != null && body.isSuccess() && body.getData() != null) {
        return body.getData();
      }

      try {
        ClientDto direct = rest.getForObject(url, ClientDto.class);
        if (direct != null && direct.getId() != null) return direct;
      } catch (Exception ignore) {}

      return null;

    } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
      return null; // 404 => no cachea por el unless
    } catch (Exception ex) {
      throw ex; // retry/CB
    }
  }

  // fallback del método cacheable (misma firma + Throwable)
  private ClientDto getByIdOrNullFallback(Long id, Throwable ex) {
    return null; // degradación: no cachea (por unless)
  }

}
