package com.project.pets.domain.dto.veterinary;

import lombok.Data;

@Data
public class VeterinaryViewDto {

    private Long id;

    private String name;

    private String address;

    private String phone;

    private String schedule;

    private boolean emergencies;

    private String url;
}

