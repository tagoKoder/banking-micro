package com.sofka.tagoKoder.backend.account.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.sofka.tagoKoder.backend.account.model.dto.BankStatementDto;
import com.sofka.tagoKoder.backend.account.model.dto.BankStatementReportDto;
import com.sofka.tagoKoder.backend.account.model.dto.PageResponse;
import com.sofka.tagoKoder.backend.account.model.dto.TransactionDto;

public interface TransactionService {

    public PageResponse<TransactionDto> getAll(Pageable pageable);
	public TransactionDto getById(Long id);
	public TransactionDto create(TransactionDto transactionDto, double accInitialAmount);
    public PageResponse<BankStatementDto> getStatementPage(
    Long clientId, LocalDate start, LocalDate end, Pageable pageable);
    public TransactionDto getLastByAccountId(Long accountId);
}
