package com.sofka.tagoKoder.backend.account.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.sofka.tagoKoder.backend.account.model.dto.BankStatementDto;
import com.sofka.tagoKoder.backend.account.model.dto.BankStatementReportDto;
import com.sofka.tagoKoder.backend.account.model.dto.PageResponse;
import com.sofka.tagoKoder.backend.account.model.dto.TransactionDto;

import reactor.core.publisher.Mono;

public interface TransactionService {

    public Mono<PageResponse<TransactionDto>> getAll(Pageable pageable);
    public Mono<TransactionDto> getById(Long id) ;
    public Mono<TransactionDto> create(TransactionDto transactionDto);
    public Mono<PageResponse<BankStatementDto>> getStatementPage(
      Long clientId, LocalDate start, LocalDate end, Pageable pageable);

}
