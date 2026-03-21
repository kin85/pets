package com.project.pets.domain.dto.vaccine;

import jakarta.persistence.*;
import lombok.Data;

@Data
public class VaccineViewDto {

    private Long id;

    private String name;

    private boolean optional;
}
