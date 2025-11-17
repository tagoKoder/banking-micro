package com.sofka.tagoKoder.backend.account.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
  private Long id;
  private String number;
  private String type;
  private double initialAmount;
  private boolean isActive;
  private Long clientId;
}
