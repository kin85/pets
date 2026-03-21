package com.project.pets.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@Table(
        name = "dog_vaccine",
        schema = "app",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_dog_vaccine", columnNames = {"dog_id", "vaccine_id", "applied_date"})
        })
public class DogVaccine {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hib_seq")
    @SequenceGenerator(name = "hib_seq", sequenceName = "app.hibernate_sequence", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dog_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Dog dog;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    @Column(name = "applied_date", nullable = false)
    private LocalDate appliedDate;
}
