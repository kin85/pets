package com.project.pets.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NoteSearchDto extends DatatablesCriterias {

    private Long dogId;
}

