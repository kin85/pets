package com.project.pets.service.impl;

import com.project.pets.domain.Veterinary;
import com.project.pets.domain.dto.veterinary.VeterinaryViewDto;
import com.project.pets.repository.VeterinaryRepository;
import com.project.pets.service.VeterinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VeterinaryServiceImpl implements VeterinaryService {

    private final VeterinaryRepository veterinaryRepository;

    public VeterinaryServiceImpl(VeterinaryRepository veterinaryRepository) {
        this.veterinaryRepository = veterinaryRepository;
    }

    @Override
    public VeterinaryViewDto getById(Long id) {
        Veterinary veterinary = veterinaryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinary not found"));
        return mapToViewDto(veterinary);
    }

    @Override
    public List<VeterinaryViewDto> getAll() {
        return veterinaryRepository.findAll()
                .stream()
                .map(this::mapToViewDto)
                .toList();
    }

    private VeterinaryViewDto mapToViewDto(Veterinary veterinary) {
        VeterinaryViewDto dto = new VeterinaryViewDto();
        dto.setId(veterinary.getId());
        dto.setName(veterinary.getName());
        dto.setAddress(veterinary.getAddress());
        dto.setPhone(veterinary.getPhone());
        dto.setSchedule(veterinary.getSchedule());
        dto.setEmergencies(veterinary.isEmergencies());
        dto.setUrl(veterinary.getUrl());
        return dto;
    }
}

