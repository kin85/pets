package com.project.pets.controller;

import com.project.pets.domain.dto.veterinarytreatment.VeterinaryTreatmentDto;
import com.project.pets.domain.dto.veterinarytreatment.VeterinaryTreatmentViewDto;
import com.project.pets.service.VeterinaryTreatmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veterinary-treatments")
public class VeterinaryTreatmentController {

    private final VeterinaryTreatmentService veterinaryTreatmentService;

    public VeterinaryTreatmentController(VeterinaryTreatmentService veterinaryTreatmentService) {
        this.veterinaryTreatmentService = veterinaryTreatmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody VeterinaryTreatmentDto dto) {
        return veterinaryTreatmentService.create(dto);
    }

    @GetMapping("/{id}")
    public VeterinaryTreatmentViewDto getById(@PathVariable Long id) {
        return veterinaryTreatmentService.getById(id);
    }

    @GetMapping("/visit/{visitId}")
    public List<VeterinaryTreatmentViewDto> getByVisitId(@PathVariable Long visitId) {
        return veterinaryTreatmentService.getByVisitId(visitId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @RequestBody VeterinaryTreatmentDto dto) {
        veterinaryTreatmentService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        veterinaryTreatmentService.delete(id);
    }
}

