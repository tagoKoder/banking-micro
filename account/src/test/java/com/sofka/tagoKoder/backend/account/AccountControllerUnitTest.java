package com.sofka.tagoKoder.backend.account;

import com.sofka.tagoKoder.backend.account.controller.AccountController;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.ApiResponse;
import com.sofka.tagoKoder.backend.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AccountControllerUnitTest {

    private final AccountService accountService = mock(AccountService.class);
    private final AccountController accountController = new AccountController(accountService);

    @Test
    void createAccountUnitTest() {
        // Arrange
        AccountDto newAccount     = new AccountDto(null, "number", "savings", 0.0, true, 1L);
        AccountDto createdAccount = new AccountDto(1L,   "number", "savings", 0.0, true, 1L);
        when(accountService.create(newAccount)).thenReturn(createdAccount);

        // Act
        ResponseEntity<ApiResponse<AccountDto>> response = accountController.create(newAccount);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdAccount, response.getBody().getData());
        verify(accountService).create(newAccount);
    }
}
