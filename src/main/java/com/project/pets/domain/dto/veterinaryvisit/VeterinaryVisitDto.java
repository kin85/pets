package com.project.pets.domain.dto.veterinaryvisit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VeterinaryVisitDto {

    private Long dogId;

    private Long veterinaryId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate visitDate;

    private String reason;

    private String diagnosis;

    private String observations;
}

