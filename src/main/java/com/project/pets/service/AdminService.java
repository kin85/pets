package com.project.pets.service;

import com.project.pets.domain.dto.DataSet;
import com.project.pets.domain.dto.DatatablesCriterias;
import com.project.pets.domain.dto.admin.AdminBootstrapDto;
import com.project.pets.domain.dto.vaccine.VaccineViewDto;
import com.project.pets.domain.dto.veterinary.VeterinaryViewDto;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface AdminService {

    AdminBootstrapDto getBootstrap();

    DataSet<Map<String, String>> findUsersByDatatables(DatatablesCriterias criterias);

    void updateUserAdminRole(Long userId, boolean admin, Authentication authentication);

    void deleteUser(Long userId, Authentication authentication);

    DataSet<Map<String, String>> findDogsByDatatables(DatatablesCriterias criterias);

    void deleteDog(Long dogId);

    DataSet<Map<String, String>> findVeterinariesByDatatables(DatatablesCriterias criterias);

    Long createVeterinary(VeterinaryViewDto dto);

    void updateVeterinary(Long id, VeterinaryViewDto dto);

    void deleteVeterinary(Long id);

    DataSet<Map<String, String>> findVaccinesByDatatables(DatatablesCriterias criterias);

    Long createVaccine(VaccineViewDto dto);

    void updateVaccine(Long id, VaccineViewDto dto);

    void deleteVaccine(Long id);

    DataSet<Map<String, String>> findNotesByDatatables(DatatablesCriterias criterias);

    DataSet<Map<String, String>> findVisitsByDatatables(DatatablesCriterias criterias);

    DataSet<Map<String, String>> findTreatmentsByDatatables(DatatablesCriterias criterias);

    DataSet<Map<String, String>> findDewormingByDatatables(DatatablesCriterias criterias);
}
