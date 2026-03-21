package com.project.pets.service;

import com.project.pets.domain.dto.vaccine.VaccineViewDto;

import java.util.List;

public interface VaccineService {

    List<VaccineViewDto> getAllVaccines();
}
