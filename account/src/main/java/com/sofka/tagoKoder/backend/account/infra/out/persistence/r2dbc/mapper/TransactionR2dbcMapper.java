package com.sofka.tagoKoder.backend.account.infra.out.persistence.r2dbc.mapper;

import com.sofka.tagoKoder.backend.account.domain.model.Transaction;
import com.sofka.tagoKoder.backend.account.infra.config.MappingConfig;
import com.sofka.tagoKoder.backend.account.infra.out.persistence.r2dbc.TransactionEntity;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MappingConfig.class)
public interface TransactionR2dbcMapper {

  // Entity -> Domain
  Transaction toDomain(TransactionEntity entity);

  // Domain -> Entity
  TransactionEntity toEntity(Transaction model);

  // Updates (por si luego los necesitas)
  void updateEntity(Transaction source, @MappingTarget TransactionEntity target);
  void updateDomain(TransactionEntity source, @MappingTarget Transaction target);
}
