package com.project.pets.service;

import com.project.pets.domain.dto.DogViewDto;
import com.project.pets.domain.dto.deworming.DewormingDto;
import com.project.pets.domain.dto.deworming.DewormingOverviewDto;
import com.project.pets.domain.dto.deworming.DewormingViewDto;
import com.project.pets.domain.dto.vaccine.VaccineDogDto;
import com.project.pets.domain.dto.vaccine.VaccineDogViewDto;
import com.project.pets.domain.dto.vaccine.VaccineOverviewDto;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DogService {

    Long createForUser(String name, String breed, String birthDate, String microchip, MultipartFile photo,
            Authentication authentication);

    Resource getPhotoResource(Long id);

    MediaType getPhotoMediaType(Long id);

    DogViewDto getViewById(Long id);

    void deleteById(Long id);

    DogViewDto updateForUser(Long id, String name, String breed, String birthDate, String microchip,
            MultipartFile photo);

    VaccineDogViewDto getVaccineDog(Long dogId);

    VaccineOverviewDto getVaccineOverview(Long dogId);

    Long save(VaccineDogDto vaccineDogDto);

    List<DewormingViewDto> getDeworming(Long dogId);

    DewormingOverviewDto getDewormingOverview(Long dogId);

    Long addDeworming(DewormingDto dto);
}
