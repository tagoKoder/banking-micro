package com.sofka.tagoKoder.backend.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.BankStatementDto;
import com.sofka.tagoKoder.backend.account.model.dto.TransactionDto;
@Mapper(config = MappingConfig.class)
public interface BankStamentMapper{
    @Mapping(source = "transaction.date",      target = "date")
    @Mapping(source = "clientName",            target = "client")
    @Mapping(source = "account.number",        target = "accountNumber")
    @Mapping(source = "account.type",          target = "accountType")
    @Mapping(source = "account.initialAmount", target = "initialAmount")
    @Mapping(source = "account.active",        target = "active")
    @Mapping(source = "transaction.type",      target = "transactionType")
    @Mapping(source = "transaction.amount",    target = "amount")
    @Mapping(source = "transaction.balance",   target = "balance")
    BankStatementDto toDto(TransactionDto transaction, AccountDto account, String clientName);
}