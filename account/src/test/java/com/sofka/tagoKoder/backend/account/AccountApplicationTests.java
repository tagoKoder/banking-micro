package com.sofka.tagoKoder.backend.account;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class AccountApplicationTests {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("accountdb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void r2dbcProps(DynamicPropertyRegistry r) {
    r.add("spring.r2dbc.url", () ->
        String.format("r2dbc:postgresql://%s:%d/%s",
            POSTGRES.getHost(), POSTGRES.getFirstMappedPort(), POSTGRES.getDatabaseName()));
    r.add("spring.r2dbc.username", POSTGRES::getUsername);
    r.add("spring.r2dbc.password", POSTGRES::getPassword);
    r.add("spring.sql.init.mode", () -> "never"); // evita inicialización JDBC
  }

  @Test
  void contextLoads() {}
}
