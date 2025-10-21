package com.sofka.tagoKoder.backend.client.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sofka.tagoKoder.backend.client.exception.NotFoundException;
import com.sofka.tagoKoder.backend.client.model.Client;
import com.sofka.tagoKoder.backend.client.model.dto.ClientCreateDto;
import com.sofka.tagoKoder.backend.client.model.dto.ClientDto;
import com.sofka.tagoKoder.backend.client.model.dto.PartialClientDto;
import com.sofka.tagoKoder.backend.client.repository.ClientRepository;

@Service
public class ClientServiceImpl implements ClientService {

	private final ClientRepository clientRepository;

	public ClientServiceImpl(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	@Override
	public List<ClientDto> getAll() {
		// Get all clients
		return clientRepository.findAll().stream()
				.map(ClientDto::fromEntity).collect(Collectors.toList());
	}

	@Override
	public ClientDto getById(Long id) {
		// Get clients by id
		Client cli = clientRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Client not found with id: " + id));
		return ClientDto.fromEntity(cli);
	}

	@Override
	public ClientDto create(ClientCreateDto clientDto) {
		// Create client
		return ClientDto.fromEntity(
				clientRepository.save(clientDto.toEntity()));
	}

	@Override
	public ClientDto update(Long id, ClientDto clientDto) {
		// Update client
		// Password not updated on this function, just the rest of the client info
		Client cli = clientRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Client not found with id: " + id));
		Client cliUpdate = ClientDto.toEntity(clientDto, cli.getPassword());
		cliUpdate.setId(id);
		return ClientDto.fromEntity(clientRepository.save(cliUpdate));
	}

	@Override
	public ClientDto partialUpdate(Long id, PartialClientDto partialClientDto) {
		// Partial update account
		Client cli = clientRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Client not found with id: " + id));
		cli.setId(id);
		cli.setActive(partialClientDto.isActive());
		return ClientDto.fromEntity(clientRepository.save(cli));
	}

	@Override
	public void deleteById(Long id) {
		// Validate if client exist
		Client cli = clientRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Client not found with id: " + id));
		// Soft Delete client
		cli.setActive(false);
		clientRepository.save(cli);
	}
}
