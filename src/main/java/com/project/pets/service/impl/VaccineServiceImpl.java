package com.project.pets.service.impl;

import com.project.pets.domain.Vaccine;
import com.project.pets.domain.dto.vaccine.VaccineViewDto;
import com.project.pets.repository.VaccineRepository;
import com.project.pets.service.VaccineService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class VaccineServiceImpl implements VaccineService {

    private final VaccineRepository repository;

    public VaccineServiceImpl(VaccineRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<VaccineViewDto> getAllVaccines() {
        List <Vaccine> vaccines = repository.findAll();
        List <VaccineViewDto> vaccineDtos = new ArrayList<>();

        for (Vaccine vaccine : vaccines) {
            VaccineViewDto dto = new VaccineViewDto();
            dto.setId(vaccine.getId());
            dto.setName(vaccine.getName());
            dto.setOptional(vaccine.isOptional());
            vaccineDtos.add(dto);
        }

        return vaccineDtos;
    }
}
