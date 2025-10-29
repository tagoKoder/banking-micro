package com.sofka.tagoKoder.backend.account.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.sofka.tagoKoder.backend.account.model.Account;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;

@Mapper(config = MappingConfig.class)
public interface AccountMapper {

  AccountDto toDto(Account model);

  @Mapping(target = "id", ignore = true)
  Account toModel(AccountDto dto);

  @BeanMapping(ignoreByDefault = false)
  void updateModel(AccountDto dto, @MappingTarget Account model);

  // Batch
  List<AccountDto> toDtoList(List<Account> models);
}
