package com.sofka.tagoKoder.backend.client.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sofka.tagoKoder.backend.client.exception.NotFoundException;
import com.sofka.tagoKoder.backend.client.model.dto.ApiResponse;
import com.sofka.tagoKoder.backend.client.model.dto.ClientCreateDto;
import com.sofka.tagoKoder.backend.client.model.dto.ClientDto;
import com.sofka.tagoKoder.backend.client.model.dto.PartialClientDto;
import com.sofka.tagoKoder.backend.client.service.ClientService;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

	private final ClientService clientService;

	public ClientController(ClientService clientService) {
		this.clientService = clientService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<ClientDto>>> getAll() {
		// api/clients
		// Get all clients
		return ResponseEntity.ok(
				new ApiResponse<List<ClientDto>>(true, "", clientService.getAll()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ClientDto>> get(@PathVariable Long id) {
		// api/clients/{id}
		// Get clients by id
		return ResponseEntity.ok(
				new ApiResponse<ClientDto>(true, "", clientService.getById(id)));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<ClientDto>> create(@RequestBody ClientCreateDto clientDto) {
		// api/clients
		// Create client
		return ResponseEntity.ok(
				new ApiResponse<ClientDto>(true, "", clientService.create(clientDto)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ClientDto>> update(@PathVariable Long id, @RequestBody ClientDto clientDto) {
		// api/clients/{id}
		// Update client
		return ResponseEntity.ok(
				new ApiResponse<ClientDto>(true, "", clientService.update(id, clientDto)));
	}
	@PutMapping("/partial/{id}")
	public ResponseEntity<ApiResponse<ClientDto>> partialUpdate(@PathVariable Long id,
			@RequestBody PartialClientDto partialClientDto) {
		// api/accounts/{id}
		// Partial update accounts
		return ResponseEntity.ok(
				new ApiResponse<ClientDto>(true, "", clientService.partialUpdate(id, partialClientDto)));
	}
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
		// api/clients/{id}
		// Delete client
		clientService.deleteById(id);
		return ResponseEntity.ok(
				new ApiResponse<>(true, "", null));
	}
}
