package com.sofka.tagoKoder.backend.account.application.service;

import com.sofka.tagoKoder.backend.account.domain.model.Account;
import com.sofka.tagoKoder.backend.account.domain.repository.AccountRepository;
import com.sofka.tagoKoder.backend.account.domain.service.AccountService;
import com.sofka.tagoKoder.backend.account.domain.out.ClientServicePort;
import com.sofka.tagoKoder.backend.account.domain.out.dto.ClientDto;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.core.DatabaseClient;
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
class AccountServiceIT {

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
    registry.add("spring.r2dbc.initialization-mode", () -> "never");
    registry.add("spring.flyway.enabled", () -> "false");
  }

  @Autowired AccountService accountService;           // Use case real
  @Autowired AccountRepository accountRepository;     // Adapter infra ya inyectado
  @Autowired DatabaseClient db;                       // Para schema sencillo

  @MockBean ClientServicePort clientPort;             // Puerto externo mock

  @BeforeEach
  void schema() {
    db.sql(
        "CREATE TABLE IF NOT EXISTS accounts (" +
        " id BIGSERIAL PRIMARY KEY," +
        " number VARCHAR(255)," +
        " type VARCHAR(50)," +
        " initial_amount DOUBLE PRECISION," +
        " is_active BOOLEAN," +
        " client_id BIGINT" +
        ")"
    ).fetch().rowsUpdated().then().block();

    db.sql("TRUNCATE TABLE accounts RESTART IDENTITY").fetch().rowsUpdated().then().block();
  }

  @Test
  @DisplayName("create(): persiste cuando el cliente existe (hexagonal, dominio puro)")
  void create_ok() {
    Long clientId = 10L;

    // Dominio de entrada (NO DTO)
    Account input = Account.builder()
        .clientId(clientId)
        .type("SAVINGS")
        .isActive(true)
        .initialAmount(0.0)
        .number("ACC-001")
        .build();

    Mockito.when(clientPort.getById(clientId))
        .thenReturn(Mono.just(ClientDto.builder()
            .id(clientId).dni("xxx").name("Cliente Prueba").isActive(true).build()));

    StepVerifier.create(
        accountService.create(input)
            .flatMap(saved -> accountRepository.findById(saved.getId())
                .map(inDb -> Tuples.of(saved, inDb))
            )
    )
    .assertNext(tuple -> {
      Account saved = tuple.getT1();
      Account inDb  = tuple.getT2();

      assertThat(saved.getId()).isNotNull();
      assertThat(inDb.getClientId()).isEqualTo(clientId);
      assertThat(inDb.getType()).isEqualTo("SAVINGS");
      assertThat(inDb.isActive()).isTrue();
      assertThat(inDb.getNumber()).isEqualTo("ACC-001");
    })
    .verifyComplete();
  }

  @Test
  @DisplayName("create(): lanza error si el cliente no existe (hexagonal)")
  void create_client_missing() {
    Long clientId = 777L;

    Account input = Account.builder()
        .clientId(clientId)
        .type("CHECKING")
        .isActive(true)
        .initialAmount(0.0)
        .number("ACC-XYZ")
        .build();

    Mockito.when(clientPort.getById(clientId)).thenReturn(Mono.empty());

    StepVerifier.create(accountService.create(input))
        .expectErrorSatisfies(ex -> {
          // Tu use case lanza RuntimeException con ese mensaje
          assertThat(ex).isInstanceOf(RuntimeException.class);
          assertThat(ex.getMessage()).contains("Client not found");
        })
        .verify();

    StepVerifier.create(accountRepository.countAll())
        .expectNext(0L)
        .verifyComplete();
  }
}
