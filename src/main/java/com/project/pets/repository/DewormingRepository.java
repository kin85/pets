package com.project.pets.repository;

import com.project.pets.domain.Deworming;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DewormingRepository extends JpaRepository<Deworming, Long> {

    List<Deworming> findByDogId(Long dogId);
}

