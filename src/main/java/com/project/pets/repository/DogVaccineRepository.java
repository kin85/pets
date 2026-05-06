package com.project.pets.repository;

import com.project.pets.domain.DogVaccine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DogVaccineRepository extends JpaRepository<DogVaccine, Long> {

    void deleteByDogId(Long dogId);

    List<DogVaccine> findByDogId(Long dogId);

    Optional<DogVaccine> findByIdAndDogId(Long id, Long dogId);

    boolean existsByVaccineId(Long vaccineId);
}
