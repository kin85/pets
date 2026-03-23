package com.project.pets.domain.dto.veterinaryvisit;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VeterinaryVisitDto {

    private Long dogId;

    private Long veterinaryId;

    private LocalDate visitDate;

    private String reason;

    private String diagnosis;

    private String observations;
}
