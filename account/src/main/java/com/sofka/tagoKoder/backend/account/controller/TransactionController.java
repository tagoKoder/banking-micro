package com.sofka.tagoKoder.backend.account.controller;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sofka.tagoKoder.backend.account.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.integration.client.ClientApiIntegration;
import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.ApiResponse;
import com.sofka.tagoKoder.backend.account.model.dto.BankStatementDto;
import com.sofka.tagoKoder.backend.account.model.dto.BankStatementReportDto;
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
	public ResponseEntity<ApiResponse<List<TransactionDto>>> getAll() {
		// api/transactions
		// Get all transactions
		return ResponseEntity.ok(
				new ApiResponse<List<TransactionDto>>(true, "", transactionService.getAll()));
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
	public ResponseEntity<ApiResponse<BankStatementReportDto>> report(
			@RequestParam Long clienteId,
			@RequestParam String fecha) {

		// Parseo del rango: "2025-01-01,2025-01-31"
		String[] parts = fecha.split(",");
		if (parts.length != 2) {
			throw new IllegalArgumentException("Parámetro 'fecha' inválido. Use 'YYYY-MM-DD,YYYY-MM-DD'.");
		}
		LocalDate start = LocalDate.parse(parts[0].trim());
		LocalDate end   = LocalDate.parse(parts[1].trim());

		var report = transactionService.getStatement(clienteId, start, end);
		return ResponseEntity.ok(new ApiResponse<>(true, "", report));
	}
}
