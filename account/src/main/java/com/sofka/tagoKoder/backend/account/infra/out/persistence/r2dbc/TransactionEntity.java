package com.sofka.tagoKoder.backend.account.infra.out.persistence.r2dbc;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table("transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEntity {
	@Id
	private Long id;
	@Column("date")
	private LocalDateTime date;
	@Column("type")
	private String type;
	@Column("amount")
	private double amount;
	@Column("balance")
	private double balance;
	@Column("account_id")
	private Long accountId;
}
