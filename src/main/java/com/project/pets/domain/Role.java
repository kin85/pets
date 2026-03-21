package com.project.pets.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
        name = "roles",
        schema = "app",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_role_name", columnNames = {"name"})
        }
)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hib_seq")
    @SequenceGenerator(name = "hib_seq", sequenceName = "app.hibernate_sequence", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;
}

