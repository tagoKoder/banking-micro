package com.sofka.tagoKoder.backend.account.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class Base {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
}
