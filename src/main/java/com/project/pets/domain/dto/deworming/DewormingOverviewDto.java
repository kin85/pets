package com.project.pets.domain.dto.deworming;

import lombok.Data;

@Data
public class DewormingOverviewDto {

    private DewormingOverviewItemDto internalDeworming;

    private DewormingOverviewItemDto externalDeworming;
}
