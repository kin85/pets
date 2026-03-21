package com.project.pets.domain;

import com.project.pets.domain.enums.AdministrationRoute;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "veterinary_treatment", schema = "app")
public class VeterinaryTreatment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hib_seq")
    @SequenceGenerator(name = "hib_seq", sequenceName = "app.hibernate_sequence", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinary_visit_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private VeterinaryVisit veterinaryVisit;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "dose", length = 100)
    private String dose;

    @Column(name = "frequency", length = 100)
    private String frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "administration_route", length = 20)
    private AdministrationRoute administrationRoute;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;
}

