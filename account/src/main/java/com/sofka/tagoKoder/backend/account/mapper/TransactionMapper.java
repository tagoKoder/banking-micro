package com.sofka.tagoKoder.backend.account.mapper;

import com.sofka.tagoKoder.backend.account.model.dto.TransactionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.sofka.tagoKoder.backend.account.model.Transaction;
import com.sofka.tagoKoder.backend.account.model.dto.TransactionDto;

@Mapper(config = MappingConfig.class)
public interface TransactionMapper{
    TransactionDto toTransactionDto(Transaction transaction);
    Transaction toModel(TransactionDto transactionDto);
}