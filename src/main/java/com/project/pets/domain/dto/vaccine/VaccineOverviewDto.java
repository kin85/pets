package com.project.pets.domain.dto.vaccine;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VaccineOverviewDto {

    private String dogName;

    private List<VaccineSummaryItemDto> currentVaccines = new ArrayList<>();

    private List<VaccineSummaryItemDto> upcomingVaccines = new ArrayList<>();

    private List<VaccineSummaryItemDto> pendingVaccines = new ArrayList<>();
}
