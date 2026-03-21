package com.project.pets.service;

import com.project.pets.domain.dto.veterinarytreatment.VeterinaryTreatmentDto;
import com.project.pets.domain.dto.veterinarytreatment.VeterinaryTreatmentViewDto;

import java.util.List;

public interface VeterinaryTreatmentService {

    Long create(VeterinaryTreatmentDto dto);

    VeterinaryTreatmentViewDto getById(Long id);

    List<VeterinaryTreatmentViewDto> getByVisitId(Long visitId);

    void update(Long id, VeterinaryTreatmentDto dto);

    void delete(Long id);
}

