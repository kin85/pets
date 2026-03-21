package com.project.pets.domain.dto.deworming;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.pets.domain.enums.DewormerType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DewormingDto {

    private Long dogId;

    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate administrationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate expirationDate;

    private DewormerType type;
}

