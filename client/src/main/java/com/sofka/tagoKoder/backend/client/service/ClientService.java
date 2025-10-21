package com.sofka.tagoKoder.backend.client.service;

import java.util.List;

import com.sofka.tagoKoder.backend.client.model.dto.ClientCreateDto;
import com.sofka.tagoKoder.backend.client.model.dto.ClientDto;
import com.sofka.tagoKoder.backend.client.model.dto.PartialClientDto;

public interface ClientService {

	public List<ClientDto> getAll();
	public ClientDto getById(Long id);
	public ClientDto create(ClientCreateDto clientDto);
	public ClientDto update(Long id, ClientDto clientDto);
	public ClientDto partialUpdate(Long id, PartialClientDto partialClientDto);
	public void deleteById(Long id);
}
