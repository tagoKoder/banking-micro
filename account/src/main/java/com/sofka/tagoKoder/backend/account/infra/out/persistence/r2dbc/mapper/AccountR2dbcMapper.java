package com.sofka.tagoKoder.backend.account.infra.out.persistence.r2dbc.mapper;

import com.sofka.tagoKoder.backend.account.domain.model.Account;
import com.sofka.tagoKoder.backend.account.infra.config.MappingConfig;
import com.sofka.tagoKoder.backend.account.infra.out.persistence.r2dbc.AccountEntity;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MappingConfig.class)
public interface AccountR2dbcMapper {

    // Entity -> Domain
    Account toDomain(AccountEntity entity);

    // Domain -> Entity
    AccountEntity toEntity(Account model);

    // Updates in-place (por si luego lo necesitas)
    void updateEntity(Account source, @MappingTarget AccountEntity target);
    void updateDomain(AccountEntity source, @MappingTarget Account target);
}
