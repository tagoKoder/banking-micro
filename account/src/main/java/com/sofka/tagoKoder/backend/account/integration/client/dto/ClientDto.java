package com.sofka.tagoKoder.backend.account.integration.client.dto;

import lombok.Data;

@Data
public class ClientDto {
    private Long id;
    private String dni;
    private String name;
    private String gender;
    private int age;
    private String address;
    private String phone;
    private boolean isActive;

    public ClientDto(){
        this.id=0L;
        this.dni = "";
        this.name="";
        this.gender="";
        this.age=0;
        this.address="";
        this.phone="";
        this.isActive=false;
    }

    public ClientDto(Long id, String dni, String name, String gender, int age, String address, String phone,
            boolean isActive) {
        this.id = id;
        this.dni = dni;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.phone = phone;
        this.isActive = isActive;
    }
    
}
