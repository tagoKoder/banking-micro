package com.sofka.tagoKoder.backend.account.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.sofka.tagoKoder.backend.account.exception.InsufficientBalanceException;
import com.sofka.tagoKoder.backend.account.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.integration.client.ClientGateway;
import com.sofka.tagoKoder.backend.account.model.Transaction;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.BankStatementDto;
import com.sofka.tagoKoder.backend.account.model.dto.BankStatementReportDto;
import com.sofka.tagoKoder.backend.account.model.dto.TransactionDto;
import com.sofka.tagoKoder.backend.account.repository.TransactionRepository;
import com.sofka.tagoKoder.backend.account.model.dto.AccountWithMovementsDto;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService; 
    private final ClientGateway clientGateway;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountService accountService, ClientGateway clientGateway) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.clientGateway = clientGateway;
    }

    @Override
    public List<TransactionDto> getAll() {
        // Get all transactions
        return transactionRepository.findAll().stream()
                .map(TransactionDto::fromEntity).collect(Collectors.toList());

    }

    @Override
    public TransactionDto getById(Long id) {
        // Get transactions by id
        Transaction tr = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));
        return TransactionDto.fromEntity(tr);
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
        // Create transaction
        return TransactionDto.fromEntity(
                transactionRepository.save(
                        transactionDto.toEntity()));
    }

    @Override
    public BankStatementReportDto getStatement(Long clientId, LocalDate start, LocalDate end) {
        var client = clientGateway.getById(clientId)
            .orElseThrow(() -> new NotFoundException("Client not found with id: " + clientId));

        var accounts = accountService.getAllByClientId(clientId);

        // Convertir LocalDate -> java.util.Date para tu repositorio
        var zone = ZoneId.systemDefault();
        Date startDate = Date.from(start.atStartOfDay(zone).toInstant());
        Date endDate   = Date.from(end.atTime(LocalTime.MAX).atZone(zone).toInstant());

        List<AccountWithMovementsDto> accountBlocks = new ArrayList<>();

        for (AccountDto acc : accounts) {
            var txs = transactionRepository
                    .findByAccountIdAndDateBetweenOrderByDate(acc.getId(), startDate, endDate)
                    .stream()
                    .map(TransactionDto::fromEntity)
                    .collect(Collectors.toList());

            // Movimientos detallados para esta cuenta
            var movements = txs.stream()
                    .map(tx -> BankStatementDto.fromEntity(tx, acc, client.getName()))
                    .collect(Collectors.toList());

            // Saldo final = balance del último movimiento (o saldo inicial si no hay movimientos)
            double finalBalance = txs.isEmpty() ? acc.getInitialAmount()
                                                : txs.get(txs.size() - 1).getBalance();

            accountBlocks.add(AccountWithMovementsDto.builder()
                    .account(acc)
                    .finalBalance(finalBalance)
                    .movements(movements)
                    .build());
        }

        return BankStatementReportDto.builder()
                .clientId(clientId)
                .clientName(client.getName())
                .start(start)
                .end(end)
                .accounts(accountBlocks)
                .build();
    }

    @Override
    public TransactionDto getLastByAccountId(Long accountId) {
        var tr = transactionRepository.findFirstByAccountIdOrderByDateDesc(accountId).orElse(null);
        return tr != null ? TransactionDto.fromEntity(tr) : null;
    }

}
