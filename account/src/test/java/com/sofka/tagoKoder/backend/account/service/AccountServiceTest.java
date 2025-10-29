package com.sofka.tagoKoder.backend.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import com.sofka.tagoKoder.backend.account.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.integration.client.ClientGateway;
import com.sofka.tagoKoder.backend.account.integration.client.dto.ClientDto;
import com.sofka.tagoKoder.backend.account.mapper.AccountMapper;
import com.sofka.tagoKoder.backend.account.model.Account;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.PageResponse;
import com.sofka.tagoKoder.backend.account.repository.AccountRepository;


public class AccountServiceTest {
    private final AccountRepository accountRepository= mock(AccountRepository.class);
    private final ClientGateway clientGateway = mock(ClientGateway.class);
    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    private final AccountService accountService = new AccountServiceImpl(accountRepository, clientGateway, accountMapper);

    private Account ac1;
    private Account ac2;
    private Account ac3;

    @BeforeEach
    void setUp() {
        ac1 = new Account();
        ac2 = new Account();
        
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

    @DisplayName("AccountService.getAll(Pageable)")
    @Test
    void getAll_pageable_ok() {
        // given
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());
        when(accountRepository.findAll(pageable))
            .thenReturn(new PageImpl<>(List.of(ac1, ac2), pageable, 5)); // total=5 => 3 páginas

        // when
        PageResponse<AccountDto> page = accountService.getAll(pageable);

        // then (contenido)
        assertEquals(2, page.getContent().size());
        assertEquals(ac1.getId(), page.getContent().get(0).getId());
        assertEquals(ac2.getId(), page.getContent().get(1).getId());

        // then (metadatos)
        assertEquals(0, page.getPage());
        assertEquals(2, page.getSize());
        assertEquals(5L, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
        assertFalse(page.isLast()); // página 0 de 3

        verify(accountRepository).findAll(pageable);
        verifyNoMoreInteractions(accountRepository, clientGateway);
    }

    @Test
    @DisplayName("Test getAllByClientId - OK")
    void getAllByClientId_ok(){
        Long clientId = 1L;
        when(accountRepository.findAllByClientId(clientId)).thenReturn(List.of(ac1,ac3));
        List<AccountDto> accounts= accountService.getAllByClientId(clientId);
        assert(accounts.size() == 2);
        assert(accounts.get(0).getId().equals(ac1.getId()));
        assert(accounts.get(1).getId().equals(ac3.getId()));

        verify(accountRepository).findAllByClientId(clientId);
        verifyNoMoreInteractions(accountRepository, clientGateway);
    }

    @Test
    @DisplayName("Test create - Not Found Client when creating Account")
    void should_throw_not_found_when_account_does_not_exist(){
        Long clientId = 10L;
        AccountDto newAccount = new AccountDto();
        newAccount.setClientId(clientId);
        newAccount.setType("SAVINGS");
        newAccount.setActive(true);

        when(clientGateway.getById(clientId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> accountService.create(newAccount));
        verify(clientGateway).getById(clientId);
        verifyNoInteractions(accountRepository);
    }

    @Test
    @DisplayName("Test create - OK")
    void should_save_account_when_user_exists(){
        Long clientId = 10L;
        Long accountId = 4L;

        AccountDto newAccount = new AccountDto();
        newAccount.setClientId(clientId);
        newAccount.setType("SAVINGS");
        newAccount.setActive(true);

        when(clientGateway.getById(clientId)).thenReturn(Optional.of(new ClientDto()));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            var entity = (Account) inv.getArgument(0);
            entity.setId(accountId);
            return entity;
        });
        AccountDto res=accountService.create(newAccount);

        assert(res.getId().equals(accountId));
        verify(clientGateway).getById(clientId);
    }
}
