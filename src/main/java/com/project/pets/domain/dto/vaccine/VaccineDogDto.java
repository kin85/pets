package com.project.pets.domain.dto.vaccine;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VaccineDogDto {

    private Long dogId;

    private Long vaccineId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate applicationDate;
}
