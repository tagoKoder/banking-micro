package com.sofka.tagoKoder.backend.account.service;

import com.sofka.tagoKoder.backend.account.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.integration.client.ClientGateway;
import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.model.Account;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class AccountServiceIntegrationTest {

  @Container
  static PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void r2dbcProps(DynamicPropertyRegistry registry) {
    String r2dbcUrl = String.format(
        "r2dbc:postgresql://%s:%d/%s",
        POSTGRES.getHost(),
        POSTGRES.getFirstMappedPort(),
        POSTGRES.getDatabaseName()
    );
    registry.add("spring.r2dbc.url", () -> r2dbcUrl);
    registry.add("spring.r2dbc.username", POSTGRES::getUsername);
    registry.add("spring.r2dbc.password", POSTGRES::getPassword);

    registry.add("spring.sql.init.mode", () -> "never");
    registry.add("spring.r2dbc.initialization-mode", () -> "never");   // <--- NUEVO
    registry.add("spring.flyway.enabled", () -> "false");    
  }

  @Autowired AccountService accountService;
  @Autowired AccountRepository accountRepository;
  @Autowired R2dbcEntityTemplate template;

  @MockBean ClientGateway clientGateway;

  @BeforeEach
  void schema() {
    template.getDatabaseClient().sql(
        "CREATE TABLE IF NOT EXISTS accounts (" +
        " id BIGSERIAL PRIMARY KEY," +
        " number VARCHAR(255)," +
        " type VARCHAR(50)," +
        " initial_amount DOUBLE PRECISION," +
        " is_active BOOLEAN," +
        " client_id BIGINT" +
        ")"
    ).then().block();

    template.getDatabaseClient().sql("TRUNCATE TABLE accounts RESTART IDENTITY").then().block();
  }

  @Test
  @DisplayName("create(): persiste cuando el cliente existe")
  void create_ok() {
    Long clientId = 10L;
    AccountDto input = new AccountDto();
    input.setClientId(clientId);
    input.setType("SAVINGS");
    input.setActive(true);

    Mockito.when(clientGateway.getById(clientId)).thenReturn(Mono.just(new ClientDto()));

    StepVerifier.create(
        accountService.create(input)
          .flatMap(saved ->
              accountRepository.findById(saved.getId()).map(inDb -> Tuples.of(saved, inDb))
          )
    )
    .assertNext(tuple -> {
      AccountDto saved = tuple.getT1();
      Account inDb = tuple.getT2();
      assertThat(saved.getId()).isNotNull();
      assertThat(inDb.getClientId()).isEqualTo(clientId);
      assertThat(inDb.getType()).isEqualTo("SAVINGS");
      assertThat(inDb.isActive()).isTrue();
    })
    .verifyComplete();
  }

  @Test
  @DisplayName("create(): lanza NotFoundException si el cliente no existe")
  void create_client_missing() {
    Long clientId = 777L;
    AccountDto input = new AccountDto();
    input.setClientId(clientId);
    input.setType("CHECKING");
    input.setActive(true);

    Mockito.when(clientGateway.getById(clientId)).thenReturn(Mono.empty());

    StepVerifier.create(accountService.create(input))
        .expectErrorSatisfies(ex -> {
          assertThat(ex).isInstanceOf(NotFoundException.class);
          assertThat(ex.getMessage()).contains("Client not found");
        })
        .verify();

    StepVerifier.create(accountRepository.count())
        .expectNext(0L)
        .verifyComplete();
  }
}
