package com.project.pets.controller;

import com.project.pets.domain.dto.*;
import com.project.pets.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody NoteDto noteDto) {
        return noteService.create(noteDto);
    }

    @GetMapping("/{id}")
    public NoteViewDto getById(@PathVariable Long id) {
        return noteService.getById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @RequestBody NoteDto noteDto) {
        noteService.update(id, noteDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        noteService.delete(id);
    }


    @PostMapping(value = "/datatables", produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public DatatablesResponse<Map<String, String>> findAllNotes(@RequestBody NoteSearchDto searchDto) {
        DataSet<Map<String, String>> dataSet = noteService.findByDatatables(searchDto);
        return DatatablesResponse.build(dataSet, searchDto);
    }
}
