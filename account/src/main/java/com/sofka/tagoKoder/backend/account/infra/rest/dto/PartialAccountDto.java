package com.sofka.tagoKoder.backend.account.infra.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartialAccountDto {
	private boolean isActive;
}
