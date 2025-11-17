package com.sofka.tagoKoder.backend.account.infra.rest;

import com.sofka.tagoKoder.backend.account.domain.model.Account;
import com.sofka.tagoKoder.backend.account.domain.service.AccountService;
import com.sofka.tagoKoder.backend.account.infra.rest.dto.*;
import com.sofka.tagoKoder.backend.account.infra.rest.mapper.AccountRestMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

  private final AccountService accountService;
  private final AccountRestMapper restMapper;

  public AccountController(AccountService accountService, AccountRestMapper restMapper) {
    this.accountService = accountService;
    this.restMapper = restMapper;
  }

  @GetMapping
  public Mono<ResponseEntity<ApiResponse<PageSlice<AccountDto>>>> getAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "asc") String direction
  ) {
    return accountService.getAll(page, size, sortBy, direction)
        .map(pageResult -> {
          List<AccountDto> content = restMapper.toDtoList(pageResult.getContent());
          PageSlice<AccountDto> body = PageSlice.of(
              content,
              pageResult.getPage(),
              pageResult.getSize(),
              pageResult.getTotalElements(),
              pageResult.getTotalPages(),
              pageResult.isLast()
          );
          return ResponseEntity.ok(new ApiResponse<>(true, "", body));
        });
  }

  @GetMapping("/client/{clientId}")
  public ResponseEntity<ApiResponse<Flux<AccountDto>>> getAllByClient(@PathVariable Long clientId) {
    Flux<Account> stream = accountService.getAllByClientId(clientId); // Flux<Account>
    return ResponseEntity.ok(new ApiResponse<>(true, "", stream.map(restMapper::toDto)));
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<ApiResponse<AccountDto>>> get(@PathVariable Long id) {
    return accountService.getById(id) // Mono<Account>
        .map(acc -> ResponseEntity.ok(new ApiResponse<>(true, "", restMapper.toDto(acc))));
  }

  @PostMapping
  public Mono<ResponseEntity<ApiResponse<AccountDto>>> create(@RequestBody Mono<AccountDto> body) {
    return body
        .map(restMapper::toDomain)         // AccountDto -> Account(dom)
        .flatMap(accountService::create)   // Mono<Account>
        .map(saved -> ResponseEntity.ok(new ApiResponse<>(true, "", restMapper.toDto(saved))));
  }

  @PutMapping("/{id}")
  public Mono<ResponseEntity<ApiResponse<AccountDto>>> update(@PathVariable Long id,
                                                              @RequestBody Mono<AccountDto> body) {
    return body
        .map(restMapper::toDomain)          // Account(dom)
        .flatMap(dom -> accountService.update(id, dom))
        .map(saved -> ResponseEntity.ok(new ApiResponse<>(true, "", restMapper.toDto(saved))));
  }

  @PutMapping("/partial/{id}")
  public Mono<ResponseEntity<ApiResponse<AccountDto>>> partialUpdate(
      @PathVariable Long id,
      @RequestBody Mono<PartialAccountDto> body
  ) {
    return body
        .flatMap(dto -> accountService.partialUpdate(id, dto.isActive()))
        .map(saved -> ResponseEntity.ok(new ApiResponse<>(true, "", restMapper.toDto(saved))));
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<ApiResponse<Void>>> delete(@PathVariable Long id) {
    return accountService.deleteById(id).thenReturn(ResponseEntity.ok(new ApiResponse<>(true, "", null)));
  }
}
