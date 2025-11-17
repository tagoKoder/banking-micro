package com.sofka.tagoKoder.backend.account.infra.rest.mapper;

import com.sofka.tagoKoder.backend.account.domain.model.Account;
import com.sofka.tagoKoder.backend.account.infra.config.MappingConfig;
import com.sofka.tagoKoder.backend.account.infra.rest.dto.AccountDto;
import org.mapstruct.*;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Mapper(config = MappingConfig.class)
@Primary
public interface AccountRestMapper {

  AccountDto toDto(Account model);

  @Mapping(target = "id", ignore = true)
  Account toDomain(AccountDto dto);

  @BeanMapping(ignoreByDefault = false)
  void updateDomain(AccountDto dto, @MappingTarget Account model);

  List<AccountDto> toDtoList(List<Account> models);
}
