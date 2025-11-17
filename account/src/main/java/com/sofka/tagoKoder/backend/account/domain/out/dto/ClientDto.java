package com.sofka.tagoKoder.backend.account.domain.out.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientDto {
  private Long id;
  private String dni;
  private String name;
  private String gender;
  private int age;
  private String address;
  private String phone;
  private boolean isActive;
}

