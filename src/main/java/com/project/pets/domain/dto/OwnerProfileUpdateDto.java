package com.project.pets.domain.dto;

import lombok.Data;

@Data
public class OwnerProfileUpdateDto {

    private String email;

    private String name;

    private String address;

    private String phone;

    private String password;
}
