package com.sofka.tagoKoder.backend.account.domain.out;

import com.sofka.tagoKoder.backend.account.domain.out.dto.ClientDto;

import reactor.core.publisher.Mono;

public interface ClientServicePort {
    Mono<Boolean> existsById(Long id);
    Mono<ClientDto> getById(Long id);
}
