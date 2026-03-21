package com.project.pets.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "vaccines", schema = "app")
public class Vaccine {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hib_seq")
    @SequenceGenerator(name = "hib_seq", sequenceName = "app.hibernate_sequence", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "optional", nullable = false)
    private boolean optional;
}

