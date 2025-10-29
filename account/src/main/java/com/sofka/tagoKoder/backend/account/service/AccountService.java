package com.sofka.tagoKoder.backend.account.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.PageResponse;
import com.sofka.tagoKoder.backend.account.model.dto.PartialAccountDto;

public interface AccountService {

	PageResponse<AccountDto> getAll(Pageable pageable);

	public List<AccountDto> getAllByClientId(Long clientId);

	public AccountDto getById(Long id);

	public AccountDto create(AccountDto accountDto);

	public AccountDto update(Long id, AccountDto accountDto);

	public AccountDto partialUpdate(Long id, PartialAccountDto partialAccountDto);

	public void deleteById(Long id);
}
