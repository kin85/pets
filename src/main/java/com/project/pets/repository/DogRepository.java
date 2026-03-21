package com.project.pets.repository;

import com.project.pets.domain.Dog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DogRepository extends JpaRepository<Dog, Long> {

    @Query("SELECT d FROM Dog d WHERE d.owner.id = :ownerId")
    List<Dog> findAllByOwnerId(@Param("ownerId") Long ownerId);
}
