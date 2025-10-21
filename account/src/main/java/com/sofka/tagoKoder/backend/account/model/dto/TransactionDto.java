package com.sofka.tagoKoder.backend.account.model.dto;

import java.util.Date;

import com.sofka.tagoKoder.backend.account.model.Transaction;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionDto {

	private Long id;
	private Date date;
	private String type;
	private double amount;
	private double balance;
	private Long accountId;

	public static TransactionDto fromEntity(Transaction tr) {
		return new TransactionDto(
				tr.getId(),
				tr.getDate(),
				tr.getType(),
				tr.getAmount(),
				tr.getBalance(),
				tr.getAccountId());
	}

	public Transaction toEntity() {
		return new Transaction(
				this.getDate(),
				this.getType(),
				this.getAmount(),
				this.getBalance(),
				this.getAccountId());
	}
}
