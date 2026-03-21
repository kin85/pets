package com.project.pets.domain.dto;

import lombok.Data;

@Data
public class UserRegisterDto {

    private String username;

    private String email;

    private String password;

    private String name;

    private String address;

    private String phone;
}
