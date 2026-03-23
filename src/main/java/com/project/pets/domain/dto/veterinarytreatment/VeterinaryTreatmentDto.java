package com.project.pets.domain.dto.veterinarytreatment;

import com.project.pets.domain.enums.AdministrationRoute;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VeterinaryTreatmentDto {

    private Long veterinaryVisitId;

    private String medicineName;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private String dose;

    private String frequency;

    private AdministrationRoute administrationRoute;

    private String instructions;
}
