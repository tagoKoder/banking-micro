package com.sofka.tagoKoder.backend.account.integration.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.model.dto.ApiResponse;

@Component
public class ClientApiIntegration implements ClientGateway{
    private final RestTemplate rest;
    private final String clientServiceBaseUrl;

    public ClientApiIntegration(RestTemplate rest, @Value("${client.service.base-url}") String clientUrl) {
        this.rest = rest;
        this.clientServiceBaseUrl = clientUrl;
    }

    @Override
    public Optional<ClientDto> getById(Long id) {
        String url = UriComponentsBuilder.fromHttpUrl(clientServiceBaseUrl)
                .path("/{id}")
                .buildAndExpand(id)
                .toUriString();
        try {
            ResponseEntity<ApiResponse<ClientDto>> res = rest.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<ClientDto>>() {});
            var body = res.getBody();
            if (body == null || !body.isSuccess() || body.getData() == null) {
                return Optional.empty();
            }
            return Optional.of(body.getData());
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
