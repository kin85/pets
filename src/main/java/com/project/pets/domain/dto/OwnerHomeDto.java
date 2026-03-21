package com.project.pets.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class OwnerHomeDto {

    private String name;

    private List<DogHomeDto> dogs;
}
