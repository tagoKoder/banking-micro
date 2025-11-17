package com.sofka.tagoKoder.backend.account.infra.rest.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class AccountWithMovementsDto {
    private AccountDto account;
    private double finalBalance;
    private List<BankStatementDto> movements;
}
