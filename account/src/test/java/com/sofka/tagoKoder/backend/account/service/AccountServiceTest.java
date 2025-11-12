package com.sofka.tagoKoder.backend.account.service;

import com.sofka.tagoKoder.backend.account.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.integration.client.ClientGateway;
import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.mapper.AccountMapper;
import com.sofka.tagoKoder.backend.account.model.Account;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.PageResponse;
import com.sofka.tagoKoder.backend.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AccountServiceTest {

  private AccountRepository accountRepository;
  private ClientGateway clientGateway;
  private R2dbcEntityTemplate template;
  private AccountMapper accountMapper;

  private AccountService accountService;

  private Account ac1;
  private Account ac2;
  private Account ac3;

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    clientGateway = mock(ClientGateway.class);
    template = mock(R2dbcEntityTemplate.class);
    accountMapper = Mappers.getMapper(AccountMapper.class);

    accountService = new AccountServiceImpl(accountRepository, clientGateway, accountMapper, template);

    ac1 = new Account();
    ac1.setId(1L);
    ac1.setClientId(10L);
    ac1.setType("SAVINGS");
    ac1.setActive(true);

    ac2 = new Account();
    ac2.setId(2L);
    ac2.setClientId(20L);
    ac2.setType("CHECKING");
    ac2.setActive(true);

    ac3 = new Account();
    ac3.setId(3L);
    ac3.setClientId(10L);
    ac3.setType("CHECKING");
    ac3.setActive(true);
  }

  @DisplayName("AccountService.getAll(page,size,sort,dir) -> PageResponse<AccountDto>")
  @Test
  void getAll_pageable_ok() {
    int page = 0, size = 2;
    String sortBy = "id", direction = "ASC";

    when(template.count(any(Query.class), eq(Account.class))).thenReturn(Mono.just(5L));
    when(template.select(any(Query.class), eq(Account.class)))
        .thenReturn(Flux.just(ac1, ac2));

    // when
    Mono<PageResponse<AccountDto>> mono = accountService.getAll(page, size, sortBy, direction);

    // then
    StepVerifier.create(mono)
        .assertNext(pr -> {
          assertThat(pr.getContent()).hasSize(2);
          assertThat(pr.getContent().get(0).getId()).isEqualTo(1L);
          assertThat(pr.getContent().get(1).getId()).isEqualTo(2L);
          assertThat(pr.getPage()).isEqualTo(0);
          assertThat(pr.getSize()).isEqualTo(2);
          assertThat(pr.getTotalElements()).isEqualTo(5L);
          assertThat(pr.getTotalPages()).isEqualTo(3); // 5/2 => 3
          assertThat(pr.isLast()).isFalse();
        })
        .verifyComplete();

    verify(template).count(any(Query.class), eq(Account.class));
    verify(template).select(any(Query.class), eq(Account.class));
    verifyNoInteractions(accountRepository, clientGateway);
  }

  @Test
  @DisplayName("getAllByClientId - OK (Flux)")
  void getAllByClientId_ok() {
    Long clientId = 10L;
    when(accountRepository.findAllByClientId(clientId)).thenReturn(Flux.just(ac1, ac3));

    StepVerifier.create(accountService.getAllByClientId(clientId))
        .assertNext(dto -> assertThat(dto.getId()).isEqualTo(ac1.getId()))
        .assertNext(dto -> assertThat(dto.getId()).isEqualTo(ac3.getId()))
        .verifyComplete();

    verify(accountRepository).findAllByClientId(clientId);
    verifyNoMoreInteractions(accountRepository);
    verifyNoInteractions(clientGateway, template);
  }

  @Test
  @DisplayName("create - NotFound cuando el cliente no existe")
  void create_should_throw_not_found_when_client_not_exists() {
    Long clientId = 10L;
    AccountDto newAccount = new AccountDto();
    newAccount.setClientId(clientId);
    newAccount.setType("SAVINGS");
    newAccount.setActive(true);

    when(clientGateway.getById(clientId)).thenReturn(Mono.empty());

    StepVerifier.create(accountService.create(newAccount))
        .expectErrorSatisfies(ex -> {
          assertThat(ex).isInstanceOf(NotFoundException.class);
          assertThat(ex).hasMessageContaining("Client not found");
        })
        .verify();

    verify(clientGateway).getById(clientId);
    verifyNoInteractions(accountRepository);
  }

  @Test
  @DisplayName("create - OK cuando el cliente existe")
  void create_should_save_when_client_exists() {
    Long clientId = 10L;
    Long accountId = 4L;

    AccountDto newAccount = new AccountDto();
    newAccount.setClientId(clientId);
    newAccount.setType("SAVINGS");
    newAccount.setActive(true);

    when(clientGateway.getById(clientId)).thenReturn(Mono.just(new ClientDto()));

    when(accountRepository.save(ArgumentMatchers.any(Account.class)))
        .thenAnswer(inv -> {
          Account e = inv.getArgument(0);
          e.setId(accountId);
          return Mono.just(e);
        });

    StepVerifier.create(accountService.create(newAccount))
        .assertNext(savedDto -> {
          assertThat(savedDto.getId()).isEqualTo(accountId);
          assertThat(savedDto.getClientId()).isEqualTo(clientId);
          assertThat(savedDto.getType()).isEqualTo("SAVINGS");
        })
        .verifyComplete();

    verify(clientGateway).getById(clientId);
    verify(accountRepository).save(any(Account.class));
    verifyNoMoreInteractions(clientGateway, accountRepository);
    verifyNoInteractions(template);
  }
}
