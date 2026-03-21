package com.project.pets.controller;

import com.project.pets.domain.dto.vaccine.VaccineViewDto;
import com.project.pets.service.VaccineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vaccines")
public class VaccineController {

    private final VaccineService service;

    public VaccineController(VaccineService service) {
        this.service = service;
    }

    @GetMapping
    public List<VaccineViewDto> getAllVaccines() {
        return service.getAllVaccines();
    }
}
