package com.sofka.tagoKoder.backend.account.integration.client;

import java.util.Optional;
import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;

import reactor.core.publisher.Mono;

public interface ClientGateway {
  Mono<ClientDto> getById(Long id);
  Mono<ClientDto> getByIdOrNull(Long id);
}