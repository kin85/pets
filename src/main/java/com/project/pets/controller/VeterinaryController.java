package com.project.pets.controller;

import com.project.pets.domain.dto.veterinary.VeterinaryViewDto;
import com.project.pets.service.VeterinaryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veterinaries")
public class VeterinaryController {

    private final VeterinaryService veterinaryService;

    public VeterinaryController(VeterinaryService veterinaryService) {
        this.veterinaryService = veterinaryService;
    }

    @GetMapping("/{id}")
    public VeterinaryViewDto getById(@PathVariable Long id) {
        return veterinaryService.getById(id);
    }

    @GetMapping
    public List<VeterinaryViewDto> getAll() {
        return veterinaryService.getAll();
    }
}

