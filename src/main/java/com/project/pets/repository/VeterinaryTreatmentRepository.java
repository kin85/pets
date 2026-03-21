package com.project.pets.repository;

import com.project.pets.domain.VeterinaryTreatment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VeterinaryTreatmentRepository extends JpaRepository<VeterinaryTreatment, Long> {

    List<VeterinaryTreatment> findByVeterinaryVisitId(Long veterinaryVisitId);
}

