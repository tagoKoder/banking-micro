package com.sofka.tagoKoder.backend.account.controller;

import java.util.List;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sofka.tagoKoder.backend.account.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.integration.client.ClientGatewayImpl;
import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.ApiResponse;
import com.sofka.tagoKoder.backend.account.model.dto.PageResponse;
import com.sofka.tagoKoder.backend.account.model.dto.PartialAccountDto;
import com.sofka.tagoKoder.backend.account.service.AccountService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<AccountDto>>> getAll(
		@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
	) {
	var pageDto = accountService.getAll(pageable);
	return ResponseEntity.ok(new ApiResponse<>(true, "", pageDto));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<AccountDto>> get(@PathVariable Long id) {
		// api/accounts/{id}
		// Get accounts by id
		return ResponseEntity.ok(
				new ApiResponse<AccountDto>(true, "", accountService.getById(id)));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<AccountDto>> create(@RequestBody AccountDto accountDto) {
		// api/accounts
		// Create accounts
		// Validate first if the client exist
		return ResponseEntity.ok(
				new ApiResponse<AccountDto>(true, "", accountService.create(accountDto)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<AccountDto>> update(@PathVariable Long id, @RequestBody AccountDto accountDto) {
		// api/accounts/{id}
		// Update accounts
		return ResponseEntity.ok(
				new ApiResponse<AccountDto>(true, "", accountService.update(id, accountDto)));
	}

	@PutMapping("/partial/{id}")
	public ResponseEntity<ApiResponse<AccountDto>> partialUpdate(@PathVariable Long id,
			@RequestBody PartialAccountDto partialAccountDto) {
		// api/accounts/{id}
		// Partial update accounts
		return ResponseEntity.ok(
				new ApiResponse<AccountDto>(true, "", accountService.partialUpdate(id, partialAccountDto)));
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
		// api/accounts/{id}
		// Delete accounts
		accountService.deleteById(id);
		return ResponseEntity.ok(new ApiResponse<>(true, "", null));
	}
}
