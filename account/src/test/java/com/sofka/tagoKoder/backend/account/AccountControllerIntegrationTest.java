package com.sofka.tagoKoder.backend.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.tagoKoder.backend.account.controller.AccountController;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper oMapper;

    @MockBean private AccountService accountService;

    @Test
    void createAccount_returnsCreatedDto() throws Exception {
        AccountDto newAccount     = new AccountDto(null, "number", "savings", 100.0, true, 1L);
        AccountDto createdAccount = new AccountDto(10L,   "number", "savings", 100.0, true, 1L);

        when(accountService.create(any(AccountDto.class))).thenReturn(createdAccount);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(oMapper.writeValueAsString(newAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.number").value("number"))
                .andExpect(jsonPath("$.data.clientId").value(1));
    }
}
