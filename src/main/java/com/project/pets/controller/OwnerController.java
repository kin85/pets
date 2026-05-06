package com.project.pets.controller;

import com.project.pets.domain.dto.OwnerDto;
import com.project.pets.domain.dto.OwnerHomeDto;
import com.project.pets.domain.dto.OwnerProfileDto;
import com.project.pets.domain.dto.OwnerProfileUpdateDto;
import com.project.pets.service.OwnerService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {

    private final OwnerService service;

    public OwnerController(OwnerService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public OwnerDto get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Long create(@RequestBody OwnerDto ownerDto) {
        return service.save(ownerDto);
    }

    @GetMapping("/{id}/home")
    public OwnerHomeDto getOwnerHome(@PathVariable Long id){
        return service.getOwnerHome(id);
    }

    @GetMapping("/me/home")
    public OwnerHomeDto getMyHome(Authentication authentication) {
        return service.getAuthenticatedOwnerHome(authentication);
    }

    @GetMapping("/me")
    public OwnerProfileDto getMyProfile(Authentication authentication) {
        return service.getAuthenticatedProfile(authentication);
    }

    @PutMapping("/me")
    public OwnerProfileDto updateMyProfile(@RequestBody OwnerProfileUpdateDto profileUpdateDto,
                                           Authentication authentication) {
        return service.updateAuthenticatedProfile(profileUpdateDto, authentication);
    }

}
