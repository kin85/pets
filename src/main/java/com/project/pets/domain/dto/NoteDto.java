package com.project.pets.domain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NoteDto {

    private LocalDate noteDate;

    private String subject;

    private String content;

    private Long dogId;
}

