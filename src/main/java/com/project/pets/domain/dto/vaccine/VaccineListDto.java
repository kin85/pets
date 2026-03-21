package com.project.pets.domain.dto.vaccine;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VaccineListDto {

    private Long id;

    private String name;

    private boolean optional;

    private LocalDate lastApplicationDate;
}
