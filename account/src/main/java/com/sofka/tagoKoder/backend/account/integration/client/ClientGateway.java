package com.sofka.tagoKoder.backend.account.integration.client;

import java.util.Optional;
import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;

public interface ClientGateway {
    Optional<ClientDto> getById(Long id);
}