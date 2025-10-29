package com.sofka.tagoKoder.backend.account.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.ApiResponse;
import com.sofka.tagoKoder.backend.account.model.dto.BankStatementDto;
import com.sofka.tagoKoder.backend.account.model.dto.PageResponse;
import com.sofka.tagoKoder.backend.account.model.dto.TransactionDto;
import com.sofka.tagoKoder.backend.account.service.AccountService;
import com.sofka.tagoKoder.backend.account.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	private final TransactionService transactionService;
	private final AccountService accountService;

	public TransactionController(TransactionService transactionService, AccountService accountService) {
		this.transactionService = transactionService;
		this.accountService = accountService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<TransactionDto>>> getAll(
		@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
	) {
		// api/transactions
		// Get all transactions
		var pageDto = transactionService.getAll(pageable);
		return ResponseEntity.ok(
				new ApiResponse<PageResponse<TransactionDto>>(true, "", pageDto));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<TransactionDto>> get(@PathVariable Long id) {
		// api/transactions/{id}
		// Get transactions by id
		return ResponseEntity.ok(
				new ApiResponse<TransactionDto>(true, "", transactionService.getById(id)));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<TransactionDto>> create(@RequestBody TransactionDto transactionDto) {
		// api/transactions
		// Create transactions
		AccountDto a = accountService.getById(transactionDto.getAccountId());
		return ResponseEntity.ok(
				new ApiResponse<TransactionDto>(true, "",
						transactionService.create(transactionDto, a.getInitialAmount())));
	}

	@GetMapping("/reportes")
	public ResponseEntity<ApiResponse<PageResponse<BankStatementDto>>> report(
		@RequestParam("clienteId") Long clienteId,
		@RequestParam("fecha") String fecha,
		@PageableDefault(size = 20, sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {

	String[] parts = fecha.split(",");
	if (parts.length != 2) {
		throw new IllegalArgumentException("Parámetro 'fecha' inválido. Use 'YYYY-MM-DD,YYYY-MM-DD'.");
	}
	LocalDate start = LocalDate.parse(parts[0].trim());
	LocalDate end   = LocalDate.parse(parts[1].trim());

	var page = transactionService.getStatementPage(clienteId, start, end, pageable);
	return ResponseEntity.ok(new ApiResponse<>(true, "", page));
	}
}
