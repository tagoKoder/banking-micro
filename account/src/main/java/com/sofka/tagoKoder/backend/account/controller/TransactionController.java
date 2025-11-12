package com.sofka.tagoKoder.backend.account.controller;

import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sofka.tagoKoder.backend.account.model.dto.ApiResponse;
import com.sofka.tagoKoder.backend.account.model.dto.BankStatementDto;
import com.sofka.tagoKoder.backend.account.model.dto.PageResponse;
import com.sofka.tagoKoder.backend.account.model.dto.TransactionDto;
import com.sofka.tagoKoder.backend.account.service.TransactionService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @GetMapping
  public Mono<ApiResponse<PageResponse<TransactionDto>>> getAll(
      @RequestParam(defaultValue = "0") int pageNumber,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction
  ) {
    var pageable = PageRequest.of(pageNumber, size, Sort.by(direction, sort));
    return transactionService.getAll(pageable)
        .map(result -> new ApiResponse<>(true, "", result));
  }

  @GetMapping("/{id}")
  public Mono<ApiResponse<TransactionDto>> get(@PathVariable Long id) {
    return transactionService.getById(id)
        .map(dto -> new ApiResponse<>(true, "", dto));
  }

  @PostMapping
  public Mono<ApiResponse<TransactionDto>> create(@RequestBody TransactionDto transactionDto) {
    return transactionService.create(transactionDto)
        .map(dto -> new ApiResponse<>(true, "", dto));
  }

  @GetMapping("/reportes")
  public Mono<ApiResponse<PageResponse<BankStatementDto>>> report(
      @RequestParam("clienteId") Long clienteId,
      @RequestParam("fecha") String fecha,
      @RequestParam(defaultValue = "0") int pageNumber,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction
  ) {
    // Normalizar: aceptar "campo,desc" o "campo" + direction separado
    String sortProp = sort;
    Sort.Direction dir = direction;
    if (sort.contains(",")) {
      String[] s = sort.split(",");
      sortProp = s[0].trim();
      if (s.length > 1 && !s[1].isBlank()) {
        dir = Sort.Direction.fromString(s[1].trim());
      }
    }

    Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(dir, sortProp));

    String[] parts = fecha.split(",");
    if (parts.length != 2) {
      return Mono.error(new IllegalArgumentException(
          "Parámetro 'fecha' inválido. Use 'YYYY-MM-DD,YYYY-MM-DD'."));
    }
    LocalDate start = LocalDate.parse(parts[0].trim());
    LocalDate end   = LocalDate.parse(parts[1].trim());

    return transactionService.getStatementPage(clienteId, start, end, pageable)
        .map(page -> new ApiResponse<>(true, "", page));
  }

}
