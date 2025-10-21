package com.sofka.tagoKoder.backend.account.model.dto;

import com.sofka.tagoKoder.backend.account.model.Account;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountDto {

	private Long id;
	private String number;
	private String type;
	private double initialAmount;
	private boolean isActive;
	private Long clientId;

	public static AccountDto fromEntity(Account a) {
		return new AccountDto(
				a.getId(),
				a.getNumber(),
				a.getType(),
				a.getInitialAmount(),
				a.isActive(),
				a.getClientId());
	}

	public Account toEntity() {
		return new Account(
				this.getNumber(),
				this.getType(),
				this.getInitialAmount(),
				this.isActive(),
				this.getClientId());
	}
}
