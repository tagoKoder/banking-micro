package com.sofka.tagoKoder.backend.account.infra.rest;

import com.sofka.tagoKoder.backend.account.domain.model.Account;
import com.sofka.tagoKoder.backend.account.domain.model.Transaction;
import com.sofka.tagoKoder.backend.account.domain.out.ClientServicePort;
import com.sofka.tagoKoder.backend.account.domain.service.AccountService;
import com.sofka.tagoKoder.backend.account.domain.service.TransactionService;
import com.sofka.tagoKoder.backend.account.infra.rest.dto.*;
import com.sofka.tagoKoder.backend.account.infra.rest.mapper.BankStatementRestMapper;
import com.sofka.tagoKoder.backend.account.infra.rest.mapper.TransactionRestMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final TransactionService txService;
  private final AccountService accountService;
  private final TransactionRestMapper txMapper;
  private final BankStatementRestMapper bsMapper;
    private final ClientServicePort clientQuery;

  public TransactionController(TransactionService txService,
                               AccountService accountService,
                               TransactionRestMapper txMapper,
                               BankStatementRestMapper bsMapper,
                               ClientServicePort clientQuery) {
    this.txService = txService;
    this.accountService = accountService;
    this.txMapper = txMapper;
    this.bsMapper = bsMapper;
    this.clientQuery = clientQuery;
  }

  @GetMapping
  public Mono<ResponseEntity<ApiResponse<PageSlice<TransactionDto>>>> getAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "ASC") String direction
  ) {
    boolean asc = !"DESC".equalsIgnoreCase(direction);
    return txService.getAll(page, size, sortBy, asc)
        .flatMap(slice -> {
          List<TransactionDto> content = slice.getContent().stream().map(txMapper::toDto).collect(Collectors.toList());
          PageSlice<TransactionDto> body = PageSlice.of(
              content, slice.getPage(), slice.getSize(), slice.getTotalElements(), slice.getTotalPages(), slice.isLast()
          );
          return Mono.just(ResponseEntity.ok(new ApiResponse<>(true, "", body)));
        });
  }

  @GetMapping("/{id}")
  public Mono<ApiResponse<TransactionDto>> get(@PathVariable Long id) {
    return txService.getById(id).map(tx -> new ApiResponse<>(true, "", txMapper.toDto(tx)));
  }

  @PostMapping
  public Mono<ApiResponse<TransactionDto>> create(@RequestBody TransactionDto dto) {
    return txService.create(txMapper.toDomain(dto))
        .map(saved -> new ApiResponse<>(true, "", txMapper.toDto(saved)));
  }

  @GetMapping("/reportes")
  public Mono<ApiResponse<PageSlice<BankStatementDto>>> report(
      @RequestParam("clienteId") Long clienteId,
      @RequestParam("fecha") String fecha,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "date") String sortBy,
      @RequestParam(defaultValue = "ASC") String direction
  ) {
    String[] parts = fecha.split(",");
    if (parts.length != 2) {
      return Mono.error(new IllegalArgumentException("Parámetro 'fecha' inválido. Use 'YYYY-MM-DD,YYYY-MM-DD'."));
    }
    LocalDate start = LocalDate.parse(parts[0].trim());
    LocalDate end   = LocalDate.parse(parts[1].trim());
    boolean asc = !"DESC".equalsIgnoreCase(direction);

    // Pedimos al caso de uso la página de transacciones
    Mono<PageSlice<Transaction>> sliceMono = txService.getStatementPage(clienteId, start, end, page, size, sortBy, asc);

    // En paralelo, cargamos cuentas y el clientName
    Mono<List<Account>> accountsMono = accountService.getAllByClientId(clienteId).collectList();
    Mono<String> clientNameMono = clientQuery.getById(clienteId)
        .map(ci -> ci.getName() == null ? "" : ci.getName())
        .defaultIfEmpty("");

    return Mono.zip(sliceMono, accountsMono, clientNameMono)
        .map(tuple -> {
          PageSlice<Transaction> slice = tuple.getT1();
          var accById = tuple.getT2().stream().collect(java.util.stream.Collectors.toMap(Account::getId, a -> a));
          String clientName = tuple.getT3();

          List<BankStatementDto> content = slice.getContent().stream()
              .map(tx -> bsMapper.toDto(tx, accById.get(tx.getAccountId()), clientName))
              .collect(Collectors.toList());

          PageSlice<BankStatementDto> body = PageSlice.of(
              content, slice.getPage(), slice.getSize(), slice.getTotalElements(), slice.getTotalPages(), slice.isLast()
          );
          return new ApiResponse<>(true, "", body);
        });
  }

}
