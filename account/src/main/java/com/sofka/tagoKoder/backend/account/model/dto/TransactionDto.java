package com.sofka.tagoKoder.backend.account.model.dto;

import java.util.Date;

import com.sofka.tagoKoder.backend.account.model.Transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
	private Long id;
	private Date date;
	private String type;
	private double amount;
	private double balance;
	private Long accountId;
}
