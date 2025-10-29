package com.sofka.tagoKoder.backend.account.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sofka.tagoKoder.backend.account.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.integration.client.ClientGateway;
import com.sofka.tagoKoder.backend.account.mapper.AccountMapper;
import com.sofka.tagoKoder.backend.account.model.Account;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.PageResponse;
import com.sofka.tagoKoder.backend.account.model.dto.PartialAccountDto;
import com.sofka.tagoKoder.backend.account.repository.AccountRepository;

@Service
public class AccountServiceImpl implements AccountService {

        private final AccountRepository accountRepository;
        private final ClientGateway clientGateway;
        private final AccountMapper accountMapper;

        public AccountServiceImpl(AccountRepository accountRepository, ClientGateway clientGateway, AccountMapper accountMapper) {
                this.accountRepository = accountRepository;
                this.clientGateway = clientGateway;
                this.accountMapper = accountMapper;
        }

        @Override
        public PageResponse<AccountDto> getAll(Pageable pageable) {
        Page<Account> page = accountRepository.findAll(pageable);
        List<AccountDto> content = page.getContent()
                .stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
        }


        @Override
        public List<AccountDto> getAllByClientId(Long clientId) {
                return accountRepository.findAllByClientId(clientId).stream()
                                .map(accountMapper::toDto).collect(Collectors.toList());
        }

        @Override
        public AccountDto getById(Long id) {
                // Get accounts by id
                Account a = accountRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
                return accountMapper.toDto(a);
        }

        @Override
        public AccountDto create(AccountDto accountDto) {
                clientGateway.getById(accountDto.getClientId())
                        .orElseThrow(() -> new NotFoundException("Client not found: " + accountDto.getClientId()));
                Account entity = accountMapper.toModel(accountDto);
                Account saved= accountRepository.save(entity);
                // Create account
                return accountMapper.toDto(saved);
        }

        @Override
        public AccountDto update(Long id, AccountDto accountDto) {
                // Update account
                Account a = accountRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
                a.setType(accountDto.getType());
                a.setActive(accountDto.isActive());
                Account saved= accountRepository.save(a);
                return accountMapper.toDto(saved);
        }

        @Override
        public AccountDto partialUpdate(Long id, PartialAccountDto partialAccountDto) {
                // Partial update account
                Account a = accountRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
                a.setActive(partialAccountDto.isActive());
                Account saved= accountRepository.save(a);
                return accountMapper.toDto(saved);
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
