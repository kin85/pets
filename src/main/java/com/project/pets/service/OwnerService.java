package com.project.pets.service;

import com.project.pets.domain.Owner;
import com.project.pets.domain.dto.OwnerDto;
import com.project.pets.domain.dto.OwnerHomeDto;
import com.project.pets.domain.dto.OwnerProfileDto;
import com.project.pets.domain.dto.OwnerProfileUpdateDto;
import org.springframework.security.core.Authentication;

public interface OwnerService {
    OwnerDto getById(Long id);

    Long save(OwnerDto ownerDto);

    OwnerHomeDto getOwnerHome(Long id);

    OwnerHomeDto getAuthenticatedOwnerHome(Authentication authentication);

    OwnerProfileDto getAuthenticatedProfile(Authentication authentication);

    OwnerProfileDto updateAuthenticatedProfile(OwnerProfileUpdateDto profileUpdateDto, Authentication authentication);
}
