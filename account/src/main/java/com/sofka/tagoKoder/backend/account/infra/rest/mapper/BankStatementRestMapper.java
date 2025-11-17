package com.sofka.tagoKoder.backend.account.infra.rest.mapper;

import com.sofka.tagoKoder.backend.account.domain.model.Account;
import com.sofka.tagoKoder.backend.account.domain.model.Transaction;
import com.sofka.tagoKoder.backend.account.infra.config.MappingConfig;
import com.sofka.tagoKoder.backend.account.infra.rest.dto.BankStatementDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MappingConfig.class)
public interface BankStatementRestMapper {

  @Mapping(target = "date",           expression = "java(java.util.Date.from(tx.getDate().atZone(java.time.ZoneId.systemDefault()).toInstant()))")
  @Mapping(target = "client",         source = "clientName")
  @Mapping(target = "accountNumber",  source = "acc.number")
  @Mapping(target = "accountType",    source = "acc.type")
  @Mapping(target = "initialAmount",  source = "acc.initialAmount")
  @Mapping(target = "active",         source = "acc.active")
  @Mapping(target = "transactionType",source = "tx.type")
  @Mapping(target = "amount",         source = "tx.amount")
  @Mapping(target = "balance",        source = "tx.balance")
  BankStatementDto toDto(Transaction tx, Account acc, String clientName);
}
