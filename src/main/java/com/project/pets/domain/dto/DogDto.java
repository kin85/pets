package com.project.pets.domain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DogDto {

    private String name;

    private String breed;

    private LocalDate birthDate;

    private String microchip;
}
