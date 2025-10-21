package com.sofka.tagoKoder.backend.account.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sofka.tagoKoder.backend.account.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.integration.client.ClientGateway;
import com.sofka.tagoKoder.backend.account.model.Account;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.PartialAccountDto;
import com.sofka.tagoKoder.backend.account.repository.AccountRepository;

@Service
public class AccountServiceImpl implements AccountService {

        private final AccountRepository accountRepository;
        private final ClientGateway clientGateway;

        public AccountServiceImpl(AccountRepository accountRepository, ClientGateway clientGateway) {
                this.accountRepository = accountRepository;
                this.clientGateway = clientGateway;
        }

        @Override
        public List<AccountDto> getAll() {
                // Get all accounts
                return accountRepository.findAll().stream()
                                .map(AccountDto::fromEntity).collect(Collectors.toList());
        }

        @Override
        public List<AccountDto> getAllByClientId(Long clientId) {
                return accountRepository.findAllByClientId(clientId).stream()
                                .map(AccountDto::fromEntity).collect(Collectors.toList());
        }

        @Override
        public AccountDto getById(Long id) {
                // Get accounts by id
                Account a = accountRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
                return AccountDto.fromEntity(a);
        }

        @Override
        public AccountDto create(AccountDto accountDto) {
                clientGateway.getById(accountDto.getClientId())
                        .orElseThrow(() -> new NotFoundException("Client not found: " + accountDto.getClientId()));
                
                // Create account
                return AccountDto.fromEntity(
                                accountRepository.save(
                                                accountDto.toEntity()));
        }

        @Override
        public AccountDto update(Long id, AccountDto accountDto) {
                // Update account
                Account a = accountRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
                a.setType(accountDto.getType());
                a.setActive(accountDto.isActive());
                return AccountDto.fromEntity(
                                accountRepository.save(a));
        }

        @Override
        public AccountDto partialUpdate(Long id, PartialAccountDto partialAccountDto) {
                // Partial update account
                Account a = accountRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
                a.setActive(partialAccountDto.isActive());
                return AccountDto.fromEntity(
                                accountRepository.save(a));
        }

        @Override
        public void deleteById(Long id) {
                // Validate if account exist
                Account a = accountRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
                a.setActive(false);
                // Soft Delete account
                accountRepository.save(a);
        }

}
