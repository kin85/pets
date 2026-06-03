package com.project.pets.controller;

import com.project.pets.domain.dto.DataSet;
import com.project.pets.domain.dto.DatatablesCriterias;
import com.project.pets.domain.dto.DatatablesResponse;
import com.project.pets.domain.dto.admin.AdminBootstrapDto;
import com.project.pets.domain.dto.admin.AdminUserRoleUpdateDto;
import com.project.pets.domain.dto.vaccine.VaccineViewDto;
import com.project.pets.domain.dto.veterinary.VeterinaryViewDto;
import com.project.pets.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/bootstrap")
    public AdminBootstrapDto getBootstrap() {
        return adminService.getBootstrap();
    }

    @PostMapping(value = "/users/datatables", produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public DatatablesResponse<Map<String, String>> findUsers(@RequestBody DatatablesCriterias criterias) {
        return buildDatatablesResponse(adminService.findUsersByDatatables(criterias), criterias);
    }

    @PutMapping("/users/{id}/admin-role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUserAdminRole(@PathVariable Long id,
                                    @RequestBody AdminUserRoleUpdateDto dto,
                                    Authentication authentication) {
        adminService.updateUserAdminRole(id, dto.admin(), authentication);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id, Authentication authentication) {
        adminService.deleteUser(id, authentication);
    }

    @PostMapping(value = "/dogs/datatables", produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public DatatablesResponse<Map<String, String>> findDogs(@RequestBody DatatablesCriterias criterias) {
        return buildDatatablesResponse(adminService.findDogsByDatatables(criterias), criterias);
    }

    @DeleteMapping("/dogs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDog(@PathVariable Long id) {
        adminService.deleteDog(id);
    }

    @PostMapping(value = "/veterinaries/datatables", produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public DatatablesResponse<Map<String, String>> findVeterinaries(@RequestBody DatatablesCriterias criterias) {
        return buildDatatablesResponse(adminService.findVeterinariesByDatatables(criterias), criterias);
    }

    @PostMapping("/veterinaries")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createVeterinary(@RequestBody VeterinaryViewDto dto) {
        return adminService.createVeterinary(dto);
    }

    @PutMapping("/veterinaries/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateVeterinary(@PathVariable Long id, @RequestBody VeterinaryViewDto dto) {
        adminService.updateVeterinary(id, dto);
    }

    @DeleteMapping("/veterinaries/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVeterinary(@PathVariable Long id) {
        adminService.deleteVeterinary(id);
    }

    @PostMapping(value = "/vaccines/datatables", produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public DatatablesResponse<Map<String, String>> findVaccines(@RequestBody DatatablesCriterias criterias) {
        return buildDatatablesResponse(adminService.findVaccinesByDatatables(criterias), criterias);
    }

    @PostMapping("/vaccines")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createVaccine(@RequestBody VaccineViewDto dto) {
        return adminService.createVaccine(dto);
    }

    @PutMapping("/vaccines/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateVaccine(@PathVariable Long id, @RequestBody VaccineViewDto dto) {
        adminService.updateVaccine(id, dto);
    }

    @DeleteMapping("/vaccines/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVaccine(@PathVariable Long id) {
        adminService.deleteVaccine(id);
    }

    @PostMapping(value = "/visits/datatables", produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public DatatablesResponse<Map<String, String>> findVisits(@RequestBody DatatablesCriterias criterias) {
        return buildDatatablesResponse(adminService.findVisitsByDatatables(criterias), criterias);
    }

    @PostMapping(value = "/treatments/datatables", produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public DatatablesResponse<Map<String, String>> findTreatments(@RequestBody DatatablesCriterias criterias) {
        return buildDatatablesResponse(adminService.findTreatmentsByDatatables(criterias), criterias);
    }

    @PostMapping(value = "/deworming/datatables", produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public DatatablesResponse<Map<String, String>> findDeworming(@RequestBody DatatablesCriterias criterias) {
        return buildDatatablesResponse(adminService.findDewormingByDatatables(criterias), criterias);
    }

    private DatatablesResponse<Map<String, String>> buildDatatablesResponse(
            DataSet<Map<String, String>> dataSet, DatatablesCriterias criterias) {
        return DatatablesResponse.build(dataSet, criterias);
    }
}
