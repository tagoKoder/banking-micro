package com.sofka.tagoKoder.backend.account.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table("accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    private Long id;
    @Column("number")
    private String number;
    @Column("type")
    private String type;
    @Column("initial_amount")
    private double initialAmount;
    @Column("is_active")
    private boolean isActive;
    @Column("client_id")
    private Long clientId;
    
}
