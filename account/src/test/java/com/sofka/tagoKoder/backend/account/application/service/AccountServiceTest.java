package com.sofka.tagoKoder.backend.account.application.service;

import com.sofka.tagoKoder.backend.account.domain.model.Account;
import com.sofka.tagoKoder.backend.account.domain.out.ClientServicePort;
import com.sofka.tagoKoder.backend.account.domain.out.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.domain.repository.AccountRepository;
import com.sofka.tagoKoder.backend.account.domain.service.AccountService;
import com.sofka.tagoKoder.backend.account.infra.out.bus.EventBus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;

  import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AccountServiceTest {

  private AccountRepository accountRepository;
  private ClientServicePort clientPort;
  private EventBus eventBus;
  private ObjectMapper json;

  private AccountService accountService;

  private Account ac1, ac2, ac3;

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    clientPort        = mock(ClientServicePort.class);
    eventBus          = mock(EventBus.class);
    json              = new ObjectMapper();

    // Usamos el constructor real del use case hexagonal
    accountService = new AccountServiceImpl(accountRepository, clientPort, eventBus, json);

    ac1 = Account.builder().id(1L).clientId(10L).type("SAVINGS").isActive(true).initialAmount(0).number("A-1").build();
    ac2 = Account.builder().id(2L).clientId(20L).type("CHECKING").isActive(true).initialAmount(0).number("A-2").build();
    ac3 = Account.builder().id(3L).clientId(10L).type("CHECKING").isActive(true).initialAmount(0).number("A-3").build();
  }

  @DisplayName("getAll(page,size,sort,dir) -> PageSlice<Account>")
  @Test
  void getAll_pageable_ok() {
    int page = 0, size = 2;
    String sortBy = "id", direction = "ASC";

    when(accountRepository.countAll()).thenReturn(Mono.just(5L));
    when(accountRepository.findPage(page, size, sortBy, /*desc*/ false)).thenReturn(Flux.just(ac1, ac2));

    StepVerifier.create(accountService.getAll(page, size, sortBy, direction))
        .assertNext(slice -> {
          assertThat(slice.getContent()).hasSize(2);
          assertThat(slice.getContent().get(0).getId()).isEqualTo(1L);
          assertThat(slice.getContent().get(1).getId()).isEqualTo(2L);
          assertThat(slice.getPage()).isEqualTo(0);
          assertThat(slice.getSize()).isEqualTo(2);
          assertThat(slice.getTotalElements()).isEqualTo(5L);
          assertThat(slice.getTotalPages()).isEqualTo(3); // 5/2 => 3
          assertThat(slice.isLast()).isFalse();
        })
        .verifyComplete();

    verify(accountRepository).countAll();
    verify(accountRepository).findPage(page, size, sortBy, false);
    verifyNoInteractions(clientPort);
  }

  @Test
  @DisplayName("getAllByClientId - OK (Flux dominio)")
  void getAllByClientId_ok() {
    Long clientId = 10L;
    when(accountRepository.findAllByClientId(clientId)).thenReturn(Flux.just(ac1, ac3));

    StepVerifier.create(accountService.getAllByClientId(clientId))
        .assertNext(a -> assertThat(a.getId()).isEqualTo(ac1.getId()))
        .assertNext(a -> assertThat(a.getId()).isEqualTo(ac3.getId()))
        .verifyComplete();

    verify(accountRepository).findAllByClientId(clientId);
    verifyNoMoreInteractions(accountRepository);
    verifyNoInteractions(clientPort);
  }

  @Test
  @DisplayName("create - lanza error cuando el cliente no existe")
  void create_should_throw_when_client_not_exists() {
    Long clientId = 10L;
    Account newAccount = Account.builder().clientId(clientId).type("SAVINGS").isActive(true).build();

    when(clientPort.getById(clientId)).thenReturn(Mono.empty());

    StepVerifier.create(accountService.create(newAccount))
        .expectErrorSatisfies(ex -> {
          assertThat(ex).isInstanceOf(RuntimeException.class);
          assertThat(ex).hasMessageContaining("Client not found");
        })
        .verify();

    verify(clientPort).getById(clientId);
    verifyNoInteractions(accountRepository);
  }

  @Test
  @DisplayName("create - persiste cuando el cliente existe")
  void create_should_save_when_client_exists() {
    Long clientId = 10L;
    Long accountId = 4L;

    Account newAccount = Account.builder().clientId(clientId).type("SAVINGS").isActive(true).build();

    doReturn(Mono.just(
        ClientDto.builder().id(clientId).dni("x").name("Cliente").isActive(true).build()
    )).when(clientPort).getById(clientId);


    when(accountRepository.save(ArgumentMatchers.any(Account.class)))
        .thenAnswer(inv -> {
          Account e = inv.getArgument(0);
          e.setId(accountId);
          return Mono.just(e);
        });

    // EventBus.publish devuelve Mono<Void>; haz que no falle
    when(eventBus.publish(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
    Mono<Account> savedAcc= accountService.create(newAccount);
    StepVerifier.create(savedAcc)
        .assertNext(saved -> {
          assertThat(saved.getId()).isEqualTo(accountId);
          assertThat(saved.getClientId()).isEqualTo(clientId);
          assertThat(saved.getType()).isEqualTo("SAVINGS");
        })
        .verifyComplete();

    verify(clientPort).getById(clientId);
    verify(accountRepository).save(any(Account.class));
    verify(eventBus).publish(anyString(), anyString(), anyString());
    verifyNoMoreInteractions(clientPort, accountRepository, eventBus);
  }
}
