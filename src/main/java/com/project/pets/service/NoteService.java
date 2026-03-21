package com.project.pets.service;

import com.project.pets.domain.dto.*;

import java.util.Map;

public interface NoteService {

    Long create(NoteDto noteDto);

    NoteViewDto getById(Long id);

    void update(Long id, NoteDto noteDto);

    void delete(Long id);


    DataSet<Map<String, String>> findByDatatables(NoteSearchDto searchDto);
}
