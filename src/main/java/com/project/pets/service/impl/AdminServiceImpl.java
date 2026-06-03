package com.project.pets.service.impl;

import com.project.pets.domain.*;
import com.project.pets.domain.dto.DataSet;
import com.project.pets.domain.dto.DatatablesCriterias;
import com.project.pets.domain.dto.admin.AdminBootstrapDto;
import com.project.pets.domain.dto.admin.AdminOptionDto;
import com.project.pets.domain.dto.vaccine.VaccineViewDto;
import com.project.pets.domain.dto.veterinary.VeterinaryViewDto;
import com.project.pets.domain.enums.AdministrationRoute;
import com.project.pets.domain.enums.DewormerType;
import com.project.pets.repository.*;
import com.project.pets.service.AdminService;
import com.project.pets.service.DogService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OwnerRepository ownerRepository;
    private final DogRepository dogRepository;
    private final DogService dogService;
    private final VeterinaryRepository veterinaryRepository;
    private final VaccineRepository vaccineRepository;
    private final VeterinaryVisitRepository veterinaryVisitRepository;
    private final VeterinaryTreatmentRepository veterinaryTreatmentRepository;
    private final DewormingRepository dewormingRepository;
    private final DogVaccineRepository dogVaccineRepository;

    public AdminServiceImpl(UserRepository userRepository,
                            RoleRepository roleRepository,
                            OwnerRepository ownerRepository,
                            DogRepository dogRepository,
                            DogService dogService,
                            VeterinaryRepository veterinaryRepository,
                            VaccineRepository vaccineRepository,
                            VeterinaryVisitRepository veterinaryVisitRepository,
                            VeterinaryTreatmentRepository veterinaryTreatmentRepository,
                            DewormingRepository dewormingRepository,
                            DogVaccineRepository dogVaccineRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.ownerRepository = ownerRepository;
        this.dogRepository = dogRepository;
        this.dogService = dogService;
        this.veterinaryRepository = veterinaryRepository;
        this.vaccineRepository = vaccineRepository;
        this.veterinaryVisitRepository = veterinaryVisitRepository;
        this.veterinaryTreatmentRepository = veterinaryTreatmentRepository;
        this.dewormingRepository = dewormingRepository;
        this.dogVaccineRepository = dogVaccineRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminBootstrapDto getBootstrap() {
        List<AdminOptionDto> dogs = dogRepository.findAll().stream()
                .map(dog -> new AdminOptionDto(dog.getId(), dog.getName() + " · " + dog.getOwner().getName()))
                .sorted(Comparator.comparing(AdminOptionDto::label, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<AdminOptionDto> veterinaries = veterinaryRepository.findAll().stream()
                .map(veterinary -> new AdminOptionDto(veterinary.getId(), veterinary.getName()))
                .sorted(Comparator.comparing(AdminOptionDto::label, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<AdminOptionDto> visits = veterinaryVisitRepository.findAll().stream()
                .map(visit -> new AdminOptionDto(
                        visit.getId(),
                        visit.getVisitDate() + " · " + visit.getDog().getName() + " · " + visit.getVeterinary().getName()))
                .sorted(Comparator.comparing(AdminOptionDto::label, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<String> administrationRoutes = Arrays.stream(AdministrationRoute.values())
                .map(Enum::name)
                .toList();

        List<String> dewormerTypes = Arrays.stream(DewormerType.values())
                .map(Enum::name)
                .toList();

        return new AdminBootstrapDto(dogs, veterinaries, visits, administrationRoutes, dewormerTypes);
    }

    @Override
    @Transactional(readOnly = true)
    public DataSet<Map<String, String>> findUsersByDatatables(DatatablesCriterias criterias) {
        List<Map<String, String>> rows = userRepository.findAll().stream()
                .map(this::mapUserRow)
                .toList();
        return buildDataSet(rows, criterias, "username", true);
    }

    @Override
    public void updateUserAdminRole(Long userId, boolean admin, Authentication authentication) {
        User user = getUserById(userId);

        if (authentication != null && user.getUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No puedes cambiar tu propio rol de administrador desde esta pantalla");
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Role ADMIN not found"));

        boolean alreadyAdmin = hasRole(user, "ADMIN");

        if (admin && !alreadyAdmin) {
            user.getRoles().add(adminRole);
        }

        if (!admin && alreadyAdmin) {
            user.getRoles().removeIf(role -> "ADMIN".equalsIgnoreCase(role.getName()));
        }

        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId, Authentication authentication) {
        User user = getUserById(userId);

        if (authentication != null && user.getUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes borrar tu propio usuario");
        }

        ownerRepository.findByUserId(userId).ifPresent(owner -> {
            List<Dog> dogs = dogRepository.findAllByOwnerId(owner.getId());
            for (Dog dog : dogs) {
                dogService.deleteById(dog.getId());
            }
            ownerRepository.delete(owner);
        });

        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public DataSet<Map<String, String>> findDogsByDatatables(DatatablesCriterias criterias) {
        List<Map<String, String>> rows = dogRepository.findAll().stream()
                .map(this::mapDogRow)
                .toList();
        return buildDataSet(rows, criterias, "name", true);
    }

    @Override
    public void deleteDog(Long dogId) {
        if (!dogRepository.existsById(dogId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found");
        }
        dogService.deleteById(dogId);
    }

    @Override
    @Transactional(readOnly = true)
    public DataSet<Map<String, String>> findVeterinariesByDatatables(DatatablesCriterias criterias) {
        List<Map<String, String>> rows = veterinaryRepository.findAll().stream()
                .map(this::mapVeterinaryRow)
                .toList();
        return buildDataSet(rows, criterias, "name", true);
    }

    @Override
    public Long createVeterinary(VeterinaryViewDto dto) {
        Veterinary veterinary = new Veterinary();
        applyVeterinaryChanges(veterinary, dto);
        return veterinaryRepository.save(veterinary).getId();
    }

    @Override
    public void updateVeterinary(Long id, VeterinaryViewDto dto) {
        Veterinary veterinary = veterinaryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinary not found"));
        applyVeterinaryChanges(veterinary, dto);
        veterinaryRepository.save(veterinary);
    }

    @Override
    public void deleteVeterinary(Long id) {
        if (!veterinaryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinary not found");
        }
        if (veterinaryVisitRepository.existsByVeterinaryId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede borrar el veterinario porque tiene visitas asociadas");
        }
        veterinaryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DataSet<Map<String, String>> findVaccinesByDatatables(DatatablesCriterias criterias) {
        List<Map<String, String>> rows = vaccineRepository.findAll().stream()
                .map(this::mapVaccineRow)
                .toList();
        return buildDataSet(rows, criterias, "name", true);
    }

    @Override
    public Long createVaccine(VaccineViewDto dto) {
        Vaccine vaccine = new Vaccine();
        applyVaccineChanges(vaccine, dto);
        return vaccineRepository.save(vaccine).getId();
    }

    @Override
    public void updateVaccine(Long id, VaccineViewDto dto) {
        Vaccine vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaccine not found"));
        applyVaccineChanges(vaccine, dto);
        vaccineRepository.save(vaccine);
    }

    @Override
    public void deleteVaccine(Long id) {
        if (!vaccineRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaccine not found");
        }
        if (dogVaccineRepository.existsByVaccineId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede borrar la vacuna porque tiene aplicaciones asociadas");
        }
        vaccineRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DataSet<Map<String, String>> findVisitsByDatatables(DatatablesCriterias criterias) {
        List<Map<String, String>> rows = veterinaryVisitRepository.findAll().stream()
                .map(this::mapVisitRow)
                .toList();
        return buildDataSet(rows, criterias, "visitDate", false);
    }

    @Override
    @Transactional(readOnly = true)
    public DataSet<Map<String, String>> findTreatmentsByDatatables(DatatablesCriterias criterias) {
        List<Map<String, String>> rows = veterinaryTreatmentRepository.findAll().stream()
                .map(this::mapTreatmentRow)
                .toList();
        return buildDataSet(rows, criterias, "startDate", false);
    }

    @Override
    @Transactional(readOnly = true)
    public DataSet<Map<String, String>> findDewormingByDatatables(DatatablesCriterias criterias) {
        List<Map<String, String>> rows = dewormingRepository.findAll().stream()
                .map(this::mapDewormingRow)
                .toList();
        return buildDataSet(rows, criterias, "administrationDate", false);
    }

    private DataSet<Map<String, String>> buildDataSet(List<Map<String, String>> rows,
                                                      DatatablesCriterias criterias,
                                                      String defaultSortColumn,
                                                      boolean ascendingDefault) {
        long totalRecords = rows.size();

        List<Map<String, String>> filteredRows = rows.stream()
                .filter(row -> matchesSearch(row, criterias.getSearchValue()))
                .sorted(buildComparator(criterias, defaultSortColumn, ascendingDefault))
                .toList();

        int safeStart = Math.max(0, criterias.getStart());
        int safeLength = criterias.getLength() > 0 ? criterias.getLength() : filteredRows.size();
        int end = Math.min(filteredRows.size(), safeStart + safeLength);
        List<Map<String, String>> pagedRows = safeStart >= filteredRows.size()
                ? List.of()
                : filteredRows.subList(safeStart, end);

        return DataSet.of(pagedRows, totalRecords, filteredRows.size());
    }

    private boolean matchesSearch(Map<String, String> row, String searchValue) {
        if (searchValue == null || searchValue.isBlank()) {
            return true;
        }

        String normalizedSearch = searchValue.trim().toLowerCase(Locale.ROOT);
        return row.values().stream()
                .filter(Objects::nonNull)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(normalizedSearch));
    }

    private Comparator<Map<String, String>> buildComparator(DatatablesCriterias criterias,
                                                            String defaultSortColumn,
                                                            boolean ascendingDefault) {
        String column = defaultSortColumn;
        boolean ascending = ascendingDefault;

        if (criterias.getOrder() != null && !criterias.getOrder().isEmpty()) {
            DatatablesCriterias.ColumnOrder order = criterias.getOrder().get(0);
            if (criterias.getColumns() != null
                    && order.getColumn() >= 0
                    && order.getColumn() < criterias.getColumns().size()) {
                String requestedColumn = criterias.getColumns().get(order.getColumn()).getData();
                if (requestedColumn != null && !requestedColumn.isBlank()) {
                    column = requestedColumn;
                }
            }
            ascending = "asc".equalsIgnoreCase(order.getDir());
        }

        final String sortColumn = column;
        Comparator<Map<String, String>> comparator = (left, right) ->
                compareSortableValues(
                        normalizeSortableValue(left.get(sortColumn), sortColumn),
                        normalizeSortableValue(right.get(sortColumn), sortColumn)
                );

        return ascending ? comparator : comparator.reversed();
    }

    private Comparable<?> normalizeSortableValue(String value, String column) {
        if (value == null || value.isBlank()) {
            return null;
        }

        if ("id".equals(column) || column.endsWith("Id")) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
                return value.toLowerCase(Locale.ROOT);
            }
        }

        return value.toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private int compareSortableValues(Comparable left, Comparable right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }

    private void applyVeterinaryChanges(Veterinary veterinary, VeterinaryViewDto dto) {
        veterinary.setName(requireText(dto.getName(), "El nombre del veterinario es obligatorio"));
        veterinary.setAddress(requireText(dto.getAddress(), "La direccion del veterinario es obligatoria"));
        veterinary.setPhone(requireText(dto.getPhone(), "El telefono del veterinario es obligatorio"));
        veterinary.setSchedule(normalizeOptionalText(dto.getSchedule()));
        veterinary.setEmergencies(dto.isEmergencies());
        veterinary.setUrl(normalizeOptionalText(dto.getUrl()));
    }

    private void applyVaccineChanges(Vaccine vaccine, VaccineViewDto dto) {
        vaccine.setName(requireText(dto.getName(), "El nombre de la vacuna es obligatorio"));
        vaccine.setOptional(dto.isOptional());
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Map<String, String> mapUserRow(User user) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(user.getId()));
        row.put("username", safe(user.getUsername()));
        row.put("email", safe(user.getEmail()));
        row.put("isAdmin", String.valueOf(hasRole(user, "ADMIN")));
        row.put("roles", user.getRoles().stream()
                .map(Role::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .reduce((left, right) -> left + ", " + right)
                .orElse(""));
        row.put("ownerName", ownerRepository.findByUserId(user.getId())
                .map(Owner::getName)
                .orElse("Sin perfil de propietario"));
        return row;
    }

    private Map<String, String> mapDogRow(Dog dog) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(dog.getId()));
        row.put("name", safe(dog.getName()));
        row.put("breed", safe(dog.getBreed()));
        row.put("birthDate", dog.getBirthDate() != null ? dog.getBirthDate().toString() : "");
        row.put("microchip", safe(dog.getMicrochip()));
        row.put("ownerName", safe(dog.getOwner().getName()));
        return row;
    }

    private Map<String, String> mapVeterinaryRow(Veterinary veterinary) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(veterinary.getId()));
        row.put("name", safe(veterinary.getName()));
        row.put("address", safe(veterinary.getAddress()));
        row.put("phone", safe(veterinary.getPhone()));
        row.put("schedule", safe(veterinary.getSchedule()));
        row.put("emergencies", veterinary.isEmergencies() ? "Si" : "No");
        row.put("url", safe(veterinary.getUrl()));
        return row;
    }

    private Map<String, String> mapVaccineRow(Vaccine vaccine) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(vaccine.getId()));
        row.put("name", safe(vaccine.getName()));
        row.put("optional", vaccine.isOptional() ? "Si" : "No");
        return row;
    }

    private Map<String, String> mapVisitRow(VeterinaryVisit visit) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(visit.getId()));
        row.put("dogId", String.valueOf(visit.getDog().getId()));
        row.put("veterinaryId", String.valueOf(visit.getVeterinary().getId()));
        row.put("visitDate", visit.getVisitDate() != null ? visit.getVisitDate().toString() : "");
        row.put("dogName", safe(visit.getDog().getName()));
        row.put("veterinaryName", safe(visit.getVeterinary().getName()));
        row.put("reason", safe(visit.getReason()));
        row.put("diagnosis", safe(visit.getDiagnosis()));
        row.put("observations", safe(visit.getObservations()));
        return row;
    }

    private Map<String, String> mapTreatmentRow(VeterinaryTreatment treatment) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(treatment.getId()));
        row.put("visitId", String.valueOf(treatment.getVeterinaryVisit().getId()));
        row.put("dogName", safe(treatment.getVeterinaryVisit().getDog().getName()));
        row.put("veterinaryName", safe(treatment.getVeterinaryVisit().getVeterinary().getName()));
        row.put("medicineName", safe(treatment.getMedicineName()));
        row.put("description", safe(treatment.getDescription()));
        row.put("startDate", treatment.getStartDate() != null ? treatment.getStartDate().toString() : "");
        row.put("endDate", treatment.getEndDate() != null ? treatment.getEndDate().toString() : "");
        row.put("dose", safe(treatment.getDose()));
        row.put("frequency", safe(treatment.getFrequency()));
        row.put("administrationRoute", treatment.getAdministrationRoute() != null
                ? treatment.getAdministrationRoute().name()
                : "");
        row.put("instructions", safe(treatment.getInstructions()));
        return row;
    }

    private Map<String, String> mapDewormingRow(Deworming deworming) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(deworming.getId()));
        row.put("dogId", String.valueOf(deworming.getDog().getId()));
        row.put("dogName", safe(deworming.getDog().getName()));
        row.put("name", safe(deworming.getName()));
        row.put("administrationDate",
                deworming.getAdministrationDate() != null ? deworming.getAdministrationDate().toString() : "");
        row.put("expirationDate",
                deworming.getExpirationDate() != null ? deworming.getExpirationDate().toString() : "");
        row.put("type", deworming.getType() != null ? deworming.getType().name() : "");
        return row;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> roleName.equalsIgnoreCase(role.getName()));
    }
}
