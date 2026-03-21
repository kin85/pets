package com.project.pets.domain.dto.veterinaryvisit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VeterinaryVisitViewDto {

    private Long id;

    private String dogName;

    private String veterinaryName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate visitDate;

    private String reason;

    private String diagnosis;

    private String observations;
}

