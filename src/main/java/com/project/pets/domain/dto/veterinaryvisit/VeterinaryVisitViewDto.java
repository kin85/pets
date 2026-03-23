package com.project.pets.domain.dto.veterinaryvisit;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VeterinaryVisitViewDto {

    private Long id;

    private Long dogId;

    private String dogName;

    private Long veterinaryId;

    private String veterinaryName;

    private LocalDate visitDate;

    private String reason;

    private String diagnosis;

    private String observations;
}
