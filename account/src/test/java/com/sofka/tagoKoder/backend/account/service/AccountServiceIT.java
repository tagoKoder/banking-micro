package com.sofka.tagoKoder.backend.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import com.sofka.tagoKoder.backend.account.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.integration.client.ClientGateway;
import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.mapper.AccountMapper;
import com.sofka.tagoKoder.backend.account.model.Account;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.repository.AccountRepository;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountServiceIntegrationTest {

  @Container
  static PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void dataSourceProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    // Descomenta si no usas Flyway/DDL externo:
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
  }

  @Autowired AccountService accountService;
  @Autowired AccountRepository accountRepository;
  @Autowired AccountMapper accountMapper;

  // Aislamos la integración externa
  @MockBean ClientGateway clientGateway;

  @Test
  @DisplayName("create(): saves when client exists")
  void create_shouldPersist_whenClientExists() {
    // arrange
    Long clientId = 10L;
    AccountDto input = new AccountDto();
    input.setClientId(clientId);
    input.setType("SAVINGS");
    input.setActive(true);

    when(clientGateway.getById(clientId))
        .thenReturn(Optional.of(new ClientDto()));

    // act
    AccountDto saved = accountService.create(input);

    // assert
    assertThat(saved.getId()).isNotNull();
    Account inDb = accountRepository.findById(saved.getId()).orElseThrow();
    assertThat(inDb.getClientId()).isEqualTo(clientId);
    assertThat(inDb.getType()).isEqualTo("SAVINGS");
    assertThat(inDb.isActive()).isTrue();
  }

  @Test
  @DisplayName("create(): throw NotFoundException when client missing")
  void create_shouldThrow_whenClientMissing() {
    // arrange
    Long clientId = 777L;
    AccountDto input = new AccountDto();
    input.setClientId(clientId);
    input.setType("CHECKING");
    input.setActive(true);

    when(clientGateway.getById(clientId)).thenReturn(Optional.empty());

    // act + assert
    assertThrows(NotFoundException.class, () -> accountService.create(input));
    assertThat(accountRepository.count()).isZero();
  }
}
