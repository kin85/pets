package com.project.pets.controller;

import com.project.pets.domain.dto.DataSet;
import com.project.pets.domain.dto.DatatablesResponse;
import com.project.pets.domain.dto.veterinaryvisit.VeterinaryVisitDto;
import com.project.pets.domain.dto.veterinaryvisit.VeterinaryVisitSearchDto;
import com.project.pets.domain.dto.veterinaryvisit.VeterinaryVisitViewDto;
import com.project.pets.service.VeterinaryVisitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/veterinary-visits")
public class VeterinaryVisitController {

    private final VeterinaryVisitService veterinaryVisitService;

    public VeterinaryVisitController(VeterinaryVisitService veterinaryVisitService) {
        this.veterinaryVisitService = veterinaryVisitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody VeterinaryVisitDto dto) {
        return veterinaryVisitService.create(dto);
    }

    @GetMapping("/{id}")
    public VeterinaryVisitViewDto getById(@PathVariable Long id) {
        return veterinaryVisitService.getById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @RequestBody VeterinaryVisitDto dto) {
        veterinaryVisitService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        veterinaryVisitService.delete(id);
    }

    @PostMapping(value = "/datatables", produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public DatatablesResponse<Map<String, String>> findByDatatables(@RequestBody VeterinaryVisitSearchDto searchDto) {
        DataSet<Map<String, String>> dataSet = veterinaryVisitService.findByDatatables(searchDto);
        return DatatablesResponse.build(dataSet, searchDto);
    }
}

