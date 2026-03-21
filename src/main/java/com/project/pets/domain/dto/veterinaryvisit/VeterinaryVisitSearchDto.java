package com.project.pets.domain.dto.veterinaryvisit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.project.pets.domain.dto.DatatablesCriterias;

@Data
@EqualsAndHashCode(callSuper = true)
public class VeterinaryVisitSearchDto extends DatatablesCriterias {

    private Long dogId;
}

