package com.project.pets.service.impl;

import com.project.pets.domain.VeterinaryTreatment;
import com.project.pets.domain.VeterinaryVisit;
import com.project.pets.domain.dto.veterinarytreatment.VeterinaryTreatmentDto;
import com.project.pets.domain.dto.veterinarytreatment.VeterinaryTreatmentViewDto;
import com.project.pets.repository.VeterinaryTreatmentRepository;
import com.project.pets.repository.VeterinaryVisitRepository;
import com.project.pets.service.VeterinaryTreatmentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class VeterinaryTreatmentServiceImpl implements VeterinaryTreatmentService {

    private final VeterinaryTreatmentRepository treatmentRepository;
    private final VeterinaryVisitRepository visitRepository;

    public VeterinaryTreatmentServiceImpl(VeterinaryTreatmentRepository treatmentRepository,
                                          VeterinaryVisitRepository visitRepository) {
        this.treatmentRepository = treatmentRepository;
        this.visitRepository = visitRepository;
    }

    @Override
    public Long create(VeterinaryTreatmentDto dto) {
        VeterinaryVisit visit = getVisitById(dto.getVeterinaryVisitId());

        VeterinaryTreatment treatment = new VeterinaryTreatment();
        mapDtoToEntity(dto, treatment, visit);

        return treatmentRepository.save(treatment).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public VeterinaryTreatmentViewDto getById(Long id) {
        return mapToViewDto(getTreatmentById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VeterinaryTreatmentViewDto> getByVisitId(Long visitId) {
        return treatmentRepository.findByVeterinaryVisitId(visitId)
                .stream()
                .map(this::mapToViewDto)
                .toList();
    }

    @Override
    public void update(Long id, VeterinaryTreatmentDto dto) {
        VeterinaryTreatment treatment = getTreatmentById(id);

        VeterinaryVisit visit = dto.getVeterinaryVisitId() != null
                && !dto.getVeterinaryVisitId().equals(treatment.getVeterinaryVisit().getId())
                ? getVisitById(dto.getVeterinaryVisitId())
                : treatment.getVeterinaryVisit();

        mapDtoToEntity(dto, treatment, visit);
        treatmentRepository.save(treatment);
    }

    @Override
    public void delete(Long id) {
        if (!treatmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment not found");
        }
        treatmentRepository.deleteById(id);
    }

    private void mapDtoToEntity(VeterinaryTreatmentDto dto, VeterinaryTreatment treatment, VeterinaryVisit visit) {
        treatment.setVeterinaryVisit(visit);
        treatment.setMedicineName(dto.getMedicineName());
        treatment.setDescription(dto.getDescription());
        treatment.setStartDate(dto.getStartDate());
        treatment.setEndDate(dto.getEndDate());
        treatment.setDose(dto.getDose());
        treatment.setFrequency(dto.getFrequency());
        treatment.setAdministrationRoute(dto.getAdministrationRoute());
        treatment.setInstructions(dto.getInstructions());
    }

    private VeterinaryTreatment getTreatmentById(Long id) {
        return treatmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment not found"));
    }

    private VeterinaryVisit getVisitById(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinary visit not found"));
    }

    private VeterinaryTreatmentViewDto mapToViewDto(VeterinaryTreatment treatment) {
        VeterinaryTreatmentViewDto dto = new VeterinaryTreatmentViewDto();
        dto.setId(treatment.getId());
        dto.setVeterinaryVisitId(treatment.getVeterinaryVisit().getId());
        dto.setMedicineName(treatment.getMedicineName());
        dto.setDescription(treatment.getDescription());
        dto.setStartDate(treatment.getStartDate());
        dto.setEndDate(treatment.getEndDate());
        dto.setDose(treatment.getDose());
        dto.setFrequency(treatment.getFrequency());
        dto.setAdministrationRoute(treatment.getAdministrationRoute());
        dto.setInstructions(treatment.getInstructions());
        return dto;
    }
}

