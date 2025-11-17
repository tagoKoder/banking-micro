package com.sofka.tagoKoder.backend.account.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
  private Long id;
  private LocalDateTime date;
  private String type;
  private double amount;
  private double balance;
  private Long accountId;
}
