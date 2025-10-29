package com.sofka.tagoKoder.backend.account.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account extends Base {
    private String number;
    private String type;
    private double initialAmount;
    private boolean isActive;
    @Column(name = "client_id")
    private Long clientId;
}
