package com.sofka.tagoKoder.backend.account.infra.rest.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BankStatementReportDto {
    private Long clientId;
    private String clientName;
    private LocalDate start;
    private LocalDate end;
    private List<AccountWithMovementsDto> accounts;
}