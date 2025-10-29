package com.sofka.tagoKoder.backend.account.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sofka.tagoKoder.backend.account.exception.InsufficientBalanceException;
import com.sofka.tagoKoder.backend.account.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.integration.client.ClientGateway;
import com.sofka.tagoKoder.backend.account.mapper.BankStamentMapper;
import com.sofka.tagoKoder.backend.account.mapper.TransactionMapper;
import com.sofka.tagoKoder.backend.account.model.Transaction;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.BankStatementDto;
import com.sofka.tagoKoder.backend.account.model.dto.PageResponse;
import com.sofka.tagoKoder.backend.account.model.dto.TransactionDto;
import com.sofka.tagoKoder.backend.account.repository.TransactionRepository;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService; 
    private final ClientGateway clientGateway;
    private final TransactionMapper transactionMapper;
    private final BankStamentMapper bankStatementMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountService accountService, ClientGateway clientGateway, TransactionMapper transactionMapper, BankStamentMapper bankStatementMapper) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.clientGateway = clientGateway;
        this.transactionMapper = transactionMapper;
        this.bankStatementMapper = bankStatementMapper;
    }

    @Override
    public PageResponse<TransactionDto> getAll(Pageable pageable) {
        Page<Transaction> page = transactionRepository.findAll(pageable);
        List<TransactionDto> content = page.getContent()
            .stream()
            .map(transactionMapper::toTransactionDto)
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
    public TransactionDto getById(Long id) {
        // Get transactions by id
        Transaction tr = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));
        return transactionMapper.toTransactionDto(tr);
    }

    @Override
    public TransactionDto create(TransactionDto transactionDto, double accInitialAmount) {
        // Get Last Transaction
        TransactionDto lastTr = this.getLastByAccountId(transactionDto.getAccountId());
        double balance = accInitialAmount;
        if (lastTr != null) {
            balance = lastTr.getBalance();
        }
        // Throw exception when balance not enough for transaction
        if (balance + transactionDto.getAmount() < 0)
            throw new InsufficientBalanceException("Saldo no disponible");

        // Setting up balance
        transactionDto.setBalance(balance + transactionDto.getAmount());
        transactionDto.setDate(new Date());
        Transaction tr = transactionMapper.toModel(transactionDto);
        Transaction saved = transactionRepository.save(tr);
        // Create transaction
        return transactionMapper.toTransactionDto(saved);
    }


        @Override
    public PageResponse<BankStatementDto> getStatementPage(
        Long clientId, LocalDate start, LocalDate end, Pageable pageable) {

    var client = clientGateway.getById(clientId)
        .orElseThrow(() -> new NotFoundException("Client not found with id: " + clientId));

    List<AccountDto> accounts = accountService.getAllByClientId(clientId);
    if (accounts.isEmpty()) {
        return PageResponse.<BankStatementDto>builder()
            .content(List.of())
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(0)
            .totalPages(0)
            .last(true)
            .build();
    }

    var zone = ZoneId.systemDefault();
    Date startDate = Date.from(start.atStartOfDay(zone).toInstant());
    Date endDate   = Date.from(end.atTime(LocalTime.MAX).atZone(zone).toInstant());

    List<Long> accountIds = accounts.stream().map(AccountDto::getId).collect(Collectors.toList());
    Map<Long, AccountDto> accMap = accounts.stream()
        .collect(Collectors.toMap(AccountDto::getId, a -> a));

    Page<Transaction> pageTx = transactionRepository
        .findByAccountIdInAndDateBetween(accountIds, startDate, endDate, pageable);

    List<BankStatementDto> content = pageTx.getContent().stream()
        .map((Transaction tr) -> {
            AccountDto acc = accMap.get(tr.getAccountId());
            TransactionDto txDto = transactionMapper.toTransactionDto(tr);
            return bankStatementMapper.toDto(txDto, acc, client.getName());
        })
        .collect(Collectors.toList());

    return PageResponse.<BankStatementDto>builder()
        .content(content)
        .page(pageTx.getNumber())
        .size(pageTx.getSize())
        .totalElements(pageTx.getTotalElements())
        .totalPages(pageTx.getTotalPages())
        .last(pageTx.isLast())
        .build();
    }

    @Override
    public TransactionDto getLastByAccountId(Long accountId) {
        Transaction tr = transactionRepository.findFirstByAccountIdOrderByDateDesc(accountId).orElse(null);
        return tr != null ? transactionMapper.toTransactionDto(tr) : null;
    }

}
