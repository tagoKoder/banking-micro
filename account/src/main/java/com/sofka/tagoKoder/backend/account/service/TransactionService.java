package com.sofka.tagoKoder.backend.account.service;

import java.time.LocalDate;
import java.util.List;

import com.sofka.tagoKoder.backend.account.model.dto.BankStatementReportDto;
import com.sofka.tagoKoder.backend.account.model.dto.TransactionDto;

public interface TransactionService {

    public List<TransactionDto> getAll();
	public TransactionDto getById(Long id);
	public TransactionDto create(TransactionDto transactionDto, double accInitialAmount);
    BankStatementReportDto getStatement(Long clientId, LocalDate start, LocalDate end);
    public TransactionDto getLastByAccountId(Long accountId);
}
