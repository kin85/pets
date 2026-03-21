package com.project.pets.domain.dto.vaccine;

import lombok.Data;

import java.util.List;

@Data
public class VaccineDogViewDto {

    private String name;

    private List<VaccineListDto> vaccines;
}
