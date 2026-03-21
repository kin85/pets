package com.project.pets.service;

import com.project.pets.domain.Owner;
import com.project.pets.domain.dto.OwnerDto;
import com.project.pets.domain.dto.OwnerHomeDto;

public interface OwnerService {
    OwnerDto getById(Long id);

    Long save(OwnerDto ownerDto);

    OwnerHomeDto getOwnerHome(Long id);
}