package com.project.pets.repository;

import com.project.pets.domain.Veterinary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeterinaryRepository extends JpaRepository<Veterinary, Long> {
}

