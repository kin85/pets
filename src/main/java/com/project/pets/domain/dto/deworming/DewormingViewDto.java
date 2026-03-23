package com.project.pets.domain.dto.deworming;

import com.project.pets.domain.enums.DewormerType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DewormingViewDto {

    private Long id;

    private String name;

    private LocalDate administrationDate;

    private LocalDate expirationDate;

    private DewormerType type;
}
