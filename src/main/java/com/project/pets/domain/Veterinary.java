package com.project.pets.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "veterinary", schema = "app")
public class Veterinary {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hib_seq")
    @SequenceGenerator(name = "hib_seq", sequenceName = "app.hibernate_sequence", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "schedule", length = 255)
    private String schedule;

    @Column(name = "emergencies", nullable = false)
    private boolean emergencies;

    @Column(name = "url", length = 255)
    private String url;
}

