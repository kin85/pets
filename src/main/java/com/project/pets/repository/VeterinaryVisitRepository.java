package com.project.pets.repository;

import com.project.pets.domain.VeterinaryVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VeterinaryVisitRepository extends JpaRepository<VeterinaryVisit, Long> {

    List<VeterinaryVisit> findByDogId(Long dogId);
}

