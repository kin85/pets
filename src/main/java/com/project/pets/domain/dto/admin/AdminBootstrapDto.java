package com.project.pets.domain.dto.admin;

import java.util.List;

public record AdminBootstrapDto(
        List<AdminOptionDto> dogs,
        List<AdminOptionDto> veterinaries,
        List<AdminOptionDto> visits,
        List<String> administrationRoutes,
        List<String> dewormerTypes
) {
}
