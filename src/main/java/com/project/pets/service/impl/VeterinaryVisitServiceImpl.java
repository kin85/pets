package com.project.pets.service.impl;

import com.project.pets.domain.Dog;
import com.project.pets.domain.Veterinary;
import com.project.pets.domain.VeterinaryVisit;
import com.project.pets.domain.dto.DataSet;
import com.project.pets.domain.dto.DatatablesCriterias;
import com.project.pets.domain.dto.veterinaryvisit.VeterinaryVisitDto;
import com.project.pets.domain.dto.veterinaryvisit.VeterinaryVisitSearchDto;
import com.project.pets.domain.dto.veterinaryvisit.VeterinaryVisitViewDto;
import com.project.pets.repository.DogRepository;
import com.project.pets.repository.VeterinaryRepository;
import com.project.pets.repository.VeterinaryVisitRepository;
import com.project.pets.repository.impl.VeterinaryVisitRepositoryImpl;
import com.project.pets.service.VeterinaryVisitService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class VeterinaryVisitServiceImpl implements VeterinaryVisitService {

    private final VeterinaryVisitRepository veterinaryVisitRepository;
    private final VeterinaryVisitRepositoryImpl veterinaryVisitRepositoryImpl;
    private final DogRepository dogRepository;
    private final VeterinaryRepository veterinaryRepository;

    public VeterinaryVisitServiceImpl(VeterinaryVisitRepository veterinaryVisitRepository,
                                      VeterinaryVisitRepositoryImpl veterinaryVisitRepositoryImpl,
                                      DogRepository dogRepository,
                                      VeterinaryRepository veterinaryRepository) {
        this.veterinaryVisitRepository = veterinaryVisitRepository;
        this.veterinaryVisitRepositoryImpl = veterinaryVisitRepositoryImpl;
        this.dogRepository = dogRepository;
        this.veterinaryRepository = veterinaryRepository;
    }

    @Override
    public Long create(VeterinaryVisitDto dto) {
        Dog dog = dogRepository.findById(dto.getDogId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found"));

        Veterinary veterinary = veterinaryRepository.findById(dto.getVeterinaryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinary not found"));

        VeterinaryVisit visit = new VeterinaryVisit();
        visit.setDog(dog);
        visit.setVeterinary(veterinary);
        visit.setVisitDate(dto.getVisitDate());
        visit.setReason(dto.getReason());
        visit.setDiagnosis(dto.getDiagnosis());
        visit.setObservations(dto.getObservations());

        return veterinaryVisitRepository.save(visit).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public VeterinaryVisitViewDto getById(Long id) {
        VeterinaryVisit visit = getVisitById(id);
        return mapToViewDto(visit);
    }

    @Override
    public void update(Long id, VeterinaryVisitDto dto) {
        VeterinaryVisit visit = getVisitById(id);

        if (dto.getDogId() != null && !dto.getDogId().equals(visit.getDog().getId())) {
            Dog dog = dogRepository.findById(dto.getDogId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found"));
            visit.setDog(dog);
        }

        if (dto.getVeterinaryId() != null && !dto.getVeterinaryId().equals(visit.getVeterinary().getId())) {
            Veterinary veterinary = veterinaryRepository.findById(dto.getVeterinaryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinary not found"));
            visit.setVeterinary(veterinary);
        }

        if (dto.getVisitDate() != null) visit.setVisitDate(dto.getVisitDate());
        if (dto.getReason() != null) visit.setReason(dto.getReason());
        if (dto.getDiagnosis() != null) visit.setDiagnosis(dto.getDiagnosis());
        if (dto.getObservations() != null) visit.setObservations(dto.getObservations());

        veterinaryVisitRepository.save(visit);
    }

    @Override
    public void delete(Long id) {
        if (!veterinaryVisitRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinary visit not found");
        }
        veterinaryVisitRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DataSet<Map<String, String>> findByDatatables(VeterinaryVisitSearchDto searchDto) {
        DatatablesCriterias criterias = searchDto;

        List<VeterinaryVisit> visits = veterinaryVisitRepositoryImpl.findByDatatables(searchDto, criterias);
        long totalFiltered = veterinaryVisitRepositoryImpl.countByCriterias(searchDto, criterias);
        long totalRecords = veterinaryVisitRepositoryImpl.countAll();

        List<Map<String, String>> data = visits.stream()
                .map(this::mapToDataTablesMap)
                .collect(Collectors.toList());

        return DataSet.of(data, totalRecords, totalFiltered);
    }

    private VeterinaryVisit getVisitById(Long id) {
        return veterinaryVisitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinary visit not found"));
    }

    private VeterinaryVisitViewDto mapToViewDto(VeterinaryVisit visit) {
        VeterinaryVisitViewDto dto = new VeterinaryVisitViewDto();
        dto.setId(visit.getId());
        dto.setDogName(visit.getDog().getName());
        dto.setVeterinaryName(visit.getVeterinary().getName());
        dto.setVisitDate(visit.getVisitDate());
        dto.setReason(visit.getReason());
        dto.setDiagnosis(visit.getDiagnosis());
        dto.setObservations(visit.getObservations());
        return dto;
    }

    private Map<String, String> mapToDataTablesMap(VeterinaryVisit visit) {
        Map<String, String> map = new HashMap<>();
        map.put("id", visit.getId().toString());
        map.put("visitDate", visit.getVisitDate().toString());
        map.put("veterinaryName", visit.getVeterinary().getName());
        map.put("reason", visit.getReason());
        map.put("diagnosis", visit.getDiagnosis() != null ? visit.getDiagnosis() : "");
        return map;
    }
}

