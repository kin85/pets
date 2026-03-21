package com.project.pets.repository;

import com.project.pets.domain.Note;
import com.project.pets.domain.dto.DatatablesCriterias;
import com.project.pets.domain.dto.NoteSearchDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    void deleteByDogId(Long dogId);

    List<Note> findByDatatables(NoteSearchDto searchDto, DatatablesCriterias criterias);

    Long countNotesByCriterias(NoteSearchDto searchDto, DatatablesCriterias criterias);

    Long countAllNotes();
}
