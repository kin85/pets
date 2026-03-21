package com.project.pets.service.impl;

import com.project.pets.domain.Dog;
import com.project.pets.domain.Note;
import com.project.pets.domain.dto.DataSet;
import com.project.pets.domain.dto.NoteDto;
import com.project.pets.domain.dto.NoteSearchDto;
import com.project.pets.domain.dto.NoteViewDto;
import com.project.pets.domain.dto.DatatablesCriterias;
import com.project.pets.repository.DogRepository;
import com.project.pets.repository.NoteRepository;
import com.project.pets.repository.impl.NoteRepositoryImpl;
import com.project.pets.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final DogRepository dogRepository;
    private final NoteRepositoryImpl noteRepositoryImpl;

    public NoteServiceImpl(NoteRepository noteRepository, DogRepository dogRepository,
                          NoteRepositoryImpl noteRepositoryImpl) {
        this.noteRepository = noteRepository;
        this.dogRepository = dogRepository;
        this.noteRepositoryImpl = noteRepositoryImpl;
    }

    @Override
    public Long create(NoteDto noteDto) {
        Dog dog = dogRepository.findById(noteDto.getDogId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found"));

        Note note = new Note();
        note.setNoteDate(noteDto.getNoteDate());
        note.setSubject(noteDto.getSubject());
        note.setContent(noteDto.getContent());
        note.setDog(dog);

        note = noteRepository.save(note);
        return note.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public NoteViewDto getById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        return mapToViewDto(note);
    }

    @Override
    public void update(Long id, NoteDto noteDto) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        note.setNoteDate(noteDto.getNoteDate());
        note.setSubject(noteDto.getSubject());
        note.setContent(noteDto.getContent());

        if (noteDto.getDogId() != null && !noteDto.getDogId().equals(note.getDog().getId())) {
            Dog dog = dogRepository.findById(noteDto.getDogId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found"));
            note.setDog(dog);
        }

        noteRepository.save(note);
    }

    @Override
    public void delete(Long id) {
        if (!noteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found");
        }
        noteRepository.deleteById(id);
    }


    @Override
    @Transactional(readOnly = true)
    public DataSet<Map<String, String>> findByDatatables(NoteSearchDto searchDto) {
        DatatablesCriterias criterias = searchDto;

        List<Note> noteList = noteRepositoryImpl.findByDatatables(searchDto, criterias);
        long totalDisplayRecords = noteRepositoryImpl.countNotesByCriterias(searchDto, criterias);
        long totalRecords = noteRepositoryImpl.countAllNotes();

        List<Map<String, String>> data = noteList.stream()
                .map(this::mapToDataTablesMap)
                .collect(Collectors.toList());

        return DataSet.of(data, totalRecords, totalDisplayRecords);
    }

    private NoteViewDto mapToViewDto(Note note) {
        NoteViewDto dto = new NoteViewDto();
        dto.setId(note.getId());
        dto.setNoteDate(note.getNoteDate());
        dto.setSubject(note.getSubject());
        dto.setContent(note.getContent());
        dto.setDogId(note.getDog().getId());
        dto.setDogName(note.getDog().getName());
        return dto;
    }

    private Map<String, String> mapToDataTablesMap(Note note) {
        Map<String, String> map = new HashMap<>();
        map.put("id", note.getId().toString());
        map.put("noteDate", note.getNoteDate().toString());
        map.put("subject", note.getSubject());
        map.put("content", note.getContent());
        return map;
    }
}
