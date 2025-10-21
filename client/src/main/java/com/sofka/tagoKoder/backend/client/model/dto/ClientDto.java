package com.sofka.tagoKoder.backend.client.model.dto;

import com.sofka.tagoKoder.backend.client.model.Client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientDto {

	private Long id;
	private String dni;
	private String name;
	// private String password;
	private String gender;
	private int age;
	private String address;
	private String phone;
	private boolean isActive;

	public static ClientDto fromEntity(Client client) {
		return new ClientDto(
				client.getId(),
				client.getDni(),
				client.getName(),
				client.getGender(),
				client.getAge(),
				client.getAddress(),
				client.getPhone(),
				client.isActive());
	}

	public static Client toEntity(ClientDto cli, String password){
		return new Client(
			cli.getName(),
			cli.getDni(),
			password,
			cli.getGender(),
			cli.getAge(),
			cli.getAddress(),
			cli.getPhone(),
			cli.isActive()
		);
	}

}
