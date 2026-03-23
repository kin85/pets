package com.project.pets.domain.dto.deworming;

import com.project.pets.domain.enums.CoverageStatus;
import com.project.pets.domain.enums.DewormerType;
import lombok.Data;

@Data
public class DewormingOverviewItemDto {

    private DewormerType type;

    private DewormingViewDto current;

    private CoverageStatus status;

    private Long daysUntilExpiration;

    private boolean canCreate;
}
