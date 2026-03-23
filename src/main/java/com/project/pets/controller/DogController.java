package com.project.pets.controller;

import com.project.pets.domain.dto.DogViewDto;
import com.project.pets.domain.dto.deworming.DewormingDto;
import com.project.pets.domain.dto.deworming.DewormingOverviewDto;
import com.project.pets.domain.dto.deworming.DewormingViewDto;
import com.project.pets.domain.dto.vaccine.VaccineDogDto;
import com.project.pets.domain.dto.vaccine.VaccineDogViewDto;
import com.project.pets.domain.dto.vaccine.VaccineOverviewDto;
import com.project.pets.service.DogService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/dogs")
public class DogController {

    private final DogService dogService;

    public DogController(DogService dogService) {
        this.dogService = dogService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long createDog(
            @RequestParam("name") String name,
            @RequestParam("breed") String breed,
            @RequestParam("birthDate") String birthDate,
            @RequestParam("microchip") String microchip,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Authentication authentication) {
        return dogService.createForUser(name, breed, birthDate, microchip, photo, authentication);
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<Resource> getDogPhoto(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(dogService.getPhotoMediaType(id))
                .cacheControl(CacheControl.noCache())
                .body(dogService.getPhotoResource(id));
    }

    @GetMapping("/{id}")
    public DogViewDto getDogView(@PathVariable Long id) {
        return dogService.getViewById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteDog(@PathVariable Long id) {
        dogService.deleteById(id);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DogViewDto updateDog(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "breed", required = false) String breed,
            @RequestParam(value = "birthDate", required = false) String birthDate,
            @RequestParam(value = "microchip", required = false) String microchip,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        return dogService.updateForUser(id, name, breed, birthDate, microchip, photo);
    }

    @GetMapping("/{id}/vaccines")
    public VaccineDogViewDto gerVaccineDog(@PathVariable Long id){
        return dogService.getVaccineDog(id);
    }

    @GetMapping("/{id}/vaccines/overview")
    public VaccineOverviewDto getVaccineOverview(@PathVariable Long id) {
        return dogService.getVaccineOverview(id);
    }

    @PostMapping("/vaccine")
    public Long createVaccine(@RequestBody VaccineDogDto vaccineDogDto) {
        return dogService.save(vaccineDogDto);
    }

    @GetMapping("/{id}/deworming")
    public List<DewormingViewDto> getDeworming(@PathVariable Long id) {
        return dogService.getDeworming(id);
    }

    @GetMapping("/{id}/deworming/overview")
    public DewormingOverviewDto getDewormingOverview(@PathVariable Long id) {
        return dogService.getDewormingOverview(id);
    }

    @PostMapping("/deworming")
    @ResponseStatus(HttpStatus.CREATED)
    public Long addDeworming(@RequestBody DewormingDto dto) {
        return dogService.addDeworming(dto);
    }
}
