package com.sofka.tagoKoder.backend.account.infra.rest.mapper;

import com.sofka.tagoKoder.backend.account.domain.model.Transaction;
import com.sofka.tagoKoder.backend.account.infra.config.MappingConfig;
import com.sofka.tagoKoder.backend.account.infra.rest.dto.TransactionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MappingConfig.class)
public interface TransactionRestMapper {
  @Mapping(target="date", expression = "java(java.util.Date.from(tx.getDate().atZone(java.time.ZoneId.systemDefault()).toInstant()))")
  TransactionDto toDto(Transaction tx);

  @Mapping(target="date", expression = "java(dto.getDate()==null? null : dto.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())")
  Transaction toDomain(TransactionDto dto);
}
