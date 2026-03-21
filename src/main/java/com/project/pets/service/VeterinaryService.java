package com.project.pets.service;

import com.project.pets.domain.dto.veterinary.VeterinaryViewDto;

import java.util.List;

public interface VeterinaryService {

    VeterinaryViewDto getById(Long id);

    List<VeterinaryViewDto> getAll();
}

