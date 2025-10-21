package com.sofka.tagoKoder.backend.account.model.dto;

import java.util.Date;

import com.sofka.tagoKoder.backend.account.model.Account;
import com.sofka.tagoKoder.backend.account.model.Transaction;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BankStatementDto {

	private Date date;
	private String client;
	private String accountNumber;
	private String accountType;
	private double initialAmount;
	private boolean isActive;
	private String transactionType;
	private double amount;
	private double balance;

	public static BankStatementDto fromEntity(TransactionDto tr, AccountDto a, String clientName) {
		return new BankStatementDto(
				tr.getDate(),
				clientName,
				a.getNumber(),
				a.getType(),
				a.getInitialAmount(),
				a.isActive(),
				tr.getType(),
				tr.getAmount(),
				tr.getBalance());
	}

}
