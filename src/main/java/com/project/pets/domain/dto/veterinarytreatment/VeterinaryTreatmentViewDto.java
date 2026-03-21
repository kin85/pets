package com.project.pets.domain.dto.veterinarytreatment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.pets.domain.enums.AdministrationRoute;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VeterinaryTreatmentViewDto {

    private Long id;

    private Long veterinaryVisitId;

    private String medicineName;

    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate endDate;

    private String dose;

    private String frequency;

    private AdministrationRoute administrationRoute;

    private String instructions;
}

