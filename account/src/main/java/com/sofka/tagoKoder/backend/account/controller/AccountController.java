package com.sofka.tagoKoder.backend.account.controller;

import com.sofka.tagoKoder.backend.account.model.dto.*;
import com.sofka.tagoKoder.backend.account.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping
  public Mono<ResponseEntity<ApiResponse<PageResponse<AccountDto>>>> getAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "asc") String direction
  ) {
    return accountService.getAll(page, size, sortBy, direction)
        .map(pr -> ResponseEntity.ok(new ApiResponse<>(true, "", pr)));
  }

  @GetMapping("/client/{clientId}")
  public ResponseEntity<Flux<AccountDto>> getAllByClient(@PathVariable Long clientId) {
    // Si quieres envolver con ApiResponse reactivo, también se puede:
    // return accountService.getAllByClientId(clientId).collectList().map(list -> new ApiResponse<>(true, "", list));
    return ResponseEntity.ok(accountService.getAllByClientId(clientId));
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<ApiResponse<AccountDto>>> get(@PathVariable Long id) {
    return accountService.getById(id)
        .map(dto -> ResponseEntity.ok(new ApiResponse<>(true, "", dto)));
  }

  @PostMapping
  public Mono<ResponseEntity<ApiResponse<AccountDto>>> create(@RequestBody Mono<AccountDto> body) {
    return body.flatMap(accountService::create)
        .map(dto -> ResponseEntity.ok(new ApiResponse<>(true, "", dto)));
  }

  @PutMapping("/{id}")
  public Mono<ResponseEntity<ApiResponse<AccountDto>>> update(@PathVariable Long id,
                                                             @RequestBody Mono<AccountDto> body) {
    return body.flatMap(dto -> accountService.update(id, dto))
        .map(dto -> ResponseEntity.ok(new ApiResponse<>(true, "", dto)));
  }

  @PutMapping("/partial/{id}")
  public Mono<ResponseEntity<ApiResponse<AccountDto>>> partialUpdate(@PathVariable Long id,
      @RequestBody Mono<PartialAccountDto> body) {
    return body.flatMap(dto -> accountService.partialUpdate(id, dto))
        .map(dto -> ResponseEntity.ok(new ApiResponse<>(true, "", dto)));
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<ApiResponse<Void>>> delete(@PathVariable Long id) {
    return accountService.deleteById(id)
        .thenReturn(ResponseEntity.ok(new ApiResponse<>(true, "", null)));
  }
}
