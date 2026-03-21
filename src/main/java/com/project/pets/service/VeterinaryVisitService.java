package com.project.pets.service;

import com.project.pets.domain.dto.DataSet;
import com.project.pets.domain.dto.veterinaryvisit.VeterinaryVisitDto;
import com.project.pets.domain.dto.veterinaryvisit.VeterinaryVisitSearchDto;
import com.project.pets.domain.dto.veterinaryvisit.VeterinaryVisitViewDto;

import java.util.Map;

public interface VeterinaryVisitService {

    Long create(VeterinaryVisitDto dto);

    VeterinaryVisitViewDto getById(Long id);

    void update(Long id, VeterinaryVisitDto dto);

    void delete(Long id);

    DataSet<Map<String, String>> findByDatatables(VeterinaryVisitSearchDto searchDto);
}

