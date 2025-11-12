package com.sofka.tagoKoder.backend.account.service;


import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.PageResponse;
import com.sofka.tagoKoder.backend.account.model.dto.PartialAccountDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {
  Mono<PageResponse<AccountDto>> getAll(int page, int size, String sortBy, String direction);
  Flux<AccountDto> getAllByClientId(Long clientId);
  Mono<AccountDto> getById(Long id);
  Mono<AccountDto> create(AccountDto accountDto);
  Mono<AccountDto> update(Long id, AccountDto accountDto);
  Mono<AccountDto> partialUpdate(Long id, PartialAccountDto partialAccountDto);
  Mono<Void> deleteById(Long id);
}