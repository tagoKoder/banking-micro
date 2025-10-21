package com.sofka.tagoKoder.backend.client.model.dto;
import com.sofka.tagoKoder.backend.client.model.Client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientCreateDto {
    private String dni;
	private String name;
	private String password;
	private String gender;
	private int age;
	private String address;
	private String phone;
	private boolean isActive;

    public Client toEntity(){
		return new Client(
			this.getName(),
			this.getDni(),
			this.getPassword(),
			this.getGender(),
			this.getAge(),
			this.getAddress(),
			this.getPhone(),
			this.isActive()
		);
	}
}
