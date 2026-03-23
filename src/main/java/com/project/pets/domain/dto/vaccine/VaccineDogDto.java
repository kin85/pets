package com.project.pets.domain.dto.vaccine;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VaccineDogDto {

    private Long dogId;

    private Long vaccineId;

    private LocalDate applicationDate;
}
