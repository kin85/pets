package com.project.pets.controller;

import com.project.pets.service.DewormingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deworming")
public class DewormingController {

    private final DewormingService dewormingService;

    public DewormingController(DewormingService dewormingService) {
        this.dewormingService = dewormingService;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        dewormingService.delete(id);
    }
}

