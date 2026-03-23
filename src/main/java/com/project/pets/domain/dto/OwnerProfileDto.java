package com.project.pets.domain.dto;

import lombok.Data;

@Data
public class OwnerProfileDto {

    private String username;

    private String email;

    private String name;

    private String address;

    private String phone;
}
