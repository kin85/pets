package com.project.pets.service.impl;

import com.project.pets.domain.Deworming;
import com.project.pets.domain.Dog;
import com.project.pets.domain.DogVaccine;
import com.project.pets.domain.Owner;
import com.project.pets.domain.User;
import com.project.pets.domain.Vaccine;
import com.project.pets.domain.dto.DogDto;
import com.project.pets.domain.dto.DogViewDto;
import com.project.pets.domain.dto.deworming.DewormingDto;
import com.project.pets.domain.dto.deworming.DewormingOverviewDto;
import com.project.pets.domain.dto.deworming.DewormingOverviewItemDto;
import com.project.pets.domain.dto.deworming.DewormingViewDto;
import com.project.pets.domain.dto.vaccine.VaccineDogDto;
import com.project.pets.domain.dto.vaccine.VaccineDogViewDto;
import com.project.pets.domain.dto.vaccine.VaccineListDto;
import com.project.pets.domain.dto.vaccine.VaccineOverviewDto;
import com.project.pets.domain.dto.vaccine.VaccineSummaryItemDto;
import com.project.pets.domain.enums.CoverageStatus;
import com.project.pets.domain.enums.DewormerType;
import com.project.pets.repository.DewormingRepository;
import com.project.pets.repository.DogRepository;
import com.project.pets.repository.DogVaccineRepository;
import com.project.pets.repository.OwnerRepository;
import com.project.pets.repository.UserRepository;
import com.project.pets.repository.VaccineRepository;
import com.project.pets.service.DogService;
import com.project.pets.service.ImageKitStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class DogServiceImpl implements DogService {

    private static final Path DOG_UPLOAD_DIR = Paths.get("uploads", "dogs");
    private static final String IMAGEKIT_PREFIX = "imagekit|";

    private final DogRepository dogRepository;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final DogVaccineRepository dogVaccineRepository;
    private final VaccineRepository vaccineRepository;
    private final DewormingRepository dewormingRepository;
    private final ImageKitStorageService imageKitStorageService;

    public DogServiceImpl(DogRepository dogRepository, OwnerRepository ownerRepository,
                          UserRepository userRepository, DogVaccineRepository dogVaccineRepository,
                          VaccineRepository vaccineRepository, DewormingRepository dewormingRepository,
                          ImageKitStorageService imageKitStorageService) {
        this.dogRepository = dogRepository;
        this.ownerRepository = ownerRepository;
        this.userRepository = userRepository;
        this.dogVaccineRepository = dogVaccineRepository;
        this.vaccineRepository = vaccineRepository;
        this.dewormingRepository = dewormingRepository;
        this.imageKitStorageService = imageKitStorageService;
    }

    @Override
    public Long createForUser(String name, String breed, String birthDate, String microchip, MultipartFile photo,
                              Authentication authentication) {
        String username = getAuthenticatedUsername(authentication);
        String photoPath = storePhoto(photo);

        DogDto dogDto = new DogDto();
        dogDto.setName(name);
        dogDto.setBreed(breed);
        dogDto.setBirthDate(parseBirthDate(birthDate));
        dogDto.setMicrochip(microchip);

        try {
            return saveForUser(dogDto, photoPath, username);
        } catch (RuntimeException ex) {
            deleteStoredPhotoIfExists(photoPath);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getPhotoResource(Long id) {
        Dog dog = getDogById(id);
        String externalPhotoUrl = extractImageKitUrl(dog.getPhotoPath());
        if (externalPhotoUrl != null) {
            try {
                return new UrlResource(URI.create(externalPhotoUrl));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found", e);
            }
        }
        Path photoPath = resolvePhotoPath(dog);

        try {
            Resource resource = new UrlResource(photoPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid photo path", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MediaType getPhotoMediaType(Long id) {
        Dog dog = getDogById(id);
        String externalPhotoUrl = extractImageKitUrl(dog.getPhotoPath());
        if (externalPhotoUrl != null) {
            return guessMediaTypeFromUrl(externalPhotoUrl);
        }
        Path photoPath = resolvePhotoPath(dog);

        try {
            String contentType = Files.probeContentType(photoPath);
            if (contentType == null) {
                return MediaType.APPLICATION_OCTET_STREAM;
            }
            return MediaType.parseMediaType(contentType);
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    public Long saveForUser(DogDto dogDto, String photoPath, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Owner owner = ownerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));

        Dog dog = new Dog();
        dog.setName(dogDto.getName());
        dog.setBreed(dogDto.getBreed());
        dog.setBirthDate(dogDto.getBirthDate());
        dog.setMicrochip(dogDto.getMicrochip());
        dog.setOwner(owner);
        dog.setPhotoPath(photoPath);

        dog = dogRepository.save(dog);
        return dog.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public DogViewDto getViewById(Long id) {
        Dog dog = getDogById(id);

        DogViewDto dto = new DogViewDto();
        dto.setId(dog.getId());
        dto.setName(dog.getName());
        dto.setBreed(dog.getBreed());
        dto.setBirthDate(dog.getBirthDate());
        dto.setMicrochip(dog.getMicrochip());
        dto.setOwnerName(dog.getOwner().getName());
        dto.setPhotoUrl(extractImageKitUrl(dog.getPhotoPath()));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public VaccineDogViewDto getVaccineDog(Long dogId) {
        Dog dog = getDogById(dogId);

        List<DogVaccine> dogVaccines = dogVaccineRepository.findByDogId(dogId);

        List<VaccineListDto> vaccineListDtos = new ArrayList<>();
        for (DogVaccine dv : dogVaccines) {
            VaccineListDto dto = new VaccineListDto();
            dto.setId(dv.getVaccine().getId());
            dto.setName(dv.getVaccine().getName());
            dto.setOptional(dv.getVaccine().isOptional());
            dto.setLastApplicationDate(dv.getAppliedDate());
            vaccineListDtos.add(dto);
        }

        VaccineDogViewDto result = new VaccineDogViewDto();
        result.setName(dog.getName());
        result.setVaccines(vaccineListDtos);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public VaccineOverviewDto getVaccineOverview(Long dogId) {
        Dog dog = getDogById(dogId);
        List<Vaccine> allVaccines = vaccineRepository.findAll();
        Map<Long, DogVaccine> latestByVaccineId = pickLatestApplications(dogVaccineRepository.findByDogId(dogId));
        List<VaccineSummaryItemDto> currentVaccines = new ArrayList<>();
        List<VaccineSummaryItemDto> upcomingVaccines = new ArrayList<>();
        List<VaccineSummaryItemDto> pendingVaccines = new ArrayList<>();
        Set<Long> coveredIds = new LinkedHashSet<>();

        for (DogVaccine dogVaccine : latestByVaccineId.values()) {
            LocalDate nextDueDate = dogVaccine.getAppliedDate().plusYears(1);
            long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), nextDueDate);
            VaccineSummaryItemDto item = mapToVaccineSummaryItemDto(dogVaccine, nextDueDate, daysUntilDue);

            if (daysUntilDue >= 0 && daysUntilDue <= 30) {
                upcomingVaccines.add(item);
                coveredIds.add(dogVaccine.getVaccine().getId());
                continue;
            }

            if (daysUntilDue > 30) {
                currentVaccines.add(item);
                coveredIds.add(dogVaccine.getVaccine().getId());
            }
        }

        for (Vaccine vaccine : allVaccines) {
            if (!coveredIds.contains(vaccine.getId())) {
                pendingVaccines.add(mapToPendingVaccineSummaryItemDto(vaccine));
            }
        }

        currentVaccines.sort(Comparator.comparing(VaccineSummaryItemDto::getName, String.CASE_INSENSITIVE_ORDER));
        upcomingVaccines.sort(Comparator.comparing(VaccineSummaryItemDto::getDaysUntilDue));
        pendingVaccines.sort(Comparator.comparing(VaccineSummaryItemDto::getName, String.CASE_INSENSITIVE_ORDER));

        VaccineOverviewDto overview = new VaccineOverviewDto();
        overview.setDogName(dog.getName());
        overview.setCurrentVaccines(currentVaccines);
        overview.setUpcomingVaccines(upcomingVaccines);
        overview.setPendingVaccines(pendingVaccines);
        return overview;
    }

    @Override
    public Long save(VaccineDogDto vaccineDogDto) {
        Dog dog = getDogById(vaccineDogDto.getDogId());

        Vaccine vaccine = vaccineRepository.findById(vaccineDogDto.getVaccineId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaccine not found"));

        DogVaccine dogVaccine = new DogVaccine();
        dogVaccine.setDog(dog);
        dogVaccine.setVaccine(vaccine);
        dogVaccine.setAppliedDate(vaccineDogDto.getApplicationDate());

        return dogVaccineRepository.save(dogVaccine).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DewormingViewDto> getDeworming(Long dogId) {
        getDogById(dogId);
        return dewormingRepository.findByDogId(dogId)
                .stream()
                .map(this::mapToDewormingViewDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DewormingOverviewDto getDewormingOverview(Long dogId) {
        getDogById(dogId);
        List<Deworming> records = dewormingRepository.findByDogId(dogId);

        DewormingOverviewDto overview = new DewormingOverviewDto();
        overview.setInternalDeworming(buildDewormingOverviewItem(DewormerType.INTERNA, records));
        overview.setExternalDeworming(buildDewormingOverviewItem(DewormerType.EXTERNA, records));
        return overview;
    }

    @Override
    public Long addDeworming(DewormingDto dto) {
        Dog dog = getDogById(dto.getDogId());

        Deworming deworming = new Deworming();
        deworming.setDog(dog);
        deworming.setName(dto.getName());
        deworming.setAdministrationDate(dto.getAdministrationDate());
        deworming.setExpirationDate(dto.getExpirationDate());
        deworming.setType(dto.getType());

        return dewormingRepository.save(deworming).getId();
    }


    private DewormingViewDto mapToDewormingViewDto(Deworming deworming) {
        DewormingViewDto dto = new DewormingViewDto();
        dto.setId(deworming.getId());
        dto.setName(deworming.getName());
        dto.setAdministrationDate(deworming.getAdministrationDate());
        dto.setExpirationDate(deworming.getExpirationDate());
        dto.setType(deworming.getType());
        return dto;
    }

    private Map<Long, DogVaccine> pickLatestApplications(List<DogVaccine> dogVaccines) {
        Map<Long, DogVaccine> latestByVaccineId = new HashMap<>();

        for (DogVaccine dogVaccine : dogVaccines) {
            DogVaccine current = latestByVaccineId.get(dogVaccine.getVaccine().getId());
            if (current == null || dogVaccine.getAppliedDate().isAfter(current.getAppliedDate())) {
                latestByVaccineId.put(dogVaccine.getVaccine().getId(), dogVaccine);
            }
        }

        return latestByVaccineId;
    }

    private VaccineSummaryItemDto mapToVaccineSummaryItemDto(DogVaccine dogVaccine, LocalDate nextDueDate, long daysUntilDue) {
        VaccineSummaryItemDto dto = new VaccineSummaryItemDto();
        dto.setId(dogVaccine.getVaccine().getId());
        dto.setName(dogVaccine.getVaccine().getName());
        dto.setOptional(dogVaccine.getVaccine().isOptional());
        dto.setLastApplicationDate(dogVaccine.getAppliedDate());
        dto.setNextDueDate(nextDueDate);
        dto.setDaysUntilDue(daysUntilDue);
        return dto;
    }

    private VaccineSummaryItemDto mapToPendingVaccineSummaryItemDto(Vaccine vaccine) {
        VaccineSummaryItemDto dto = new VaccineSummaryItemDto();
        dto.setId(vaccine.getId());
        dto.setName(vaccine.getName());
        dto.setOptional(vaccine.isOptional());
        return dto;
    }

    private DewormingOverviewItemDto buildDewormingOverviewItem(DewormerType type, List<Deworming> records) {
        Deworming latest = pickLatestDeworming(type, records);
        DewormingOverviewItemDto dto = new DewormingOverviewItemDto();
        dto.setType(type);

        if (latest == null) {
            dto.setStatus(CoverageStatus.MISSING);
            dto.setCanCreate(true);
            return dto;
        }

        dto.setCurrent(mapToDewormingViewDto(latest));
        if (latest.getExpirationDate() == null) {
            dto.setStatus(CoverageStatus.ACTIVE);
            dto.setCanCreate(false);
            return dto;
        }

        long daysUntilExpiration = ChronoUnit.DAYS.between(LocalDate.now(), latest.getExpirationDate());
        dto.setDaysUntilExpiration(daysUntilExpiration);

        if (daysUntilExpiration < 0) {
            dto.setStatus(CoverageStatus.EXPIRED);
            dto.setCanCreate(true);
            return dto;
        }

        if (daysUntilExpiration <= 30) {
            dto.setStatus(CoverageStatus.WARNING);
            dto.setCanCreate(true);
            return dto;
        }

        dto.setStatus(CoverageStatus.ACTIVE);
        dto.setCanCreate(false);
        return dto;
    }

    private Deworming pickLatestDeworming(DewormerType type, List<Deworming> records) {
        Deworming latest = null;

        for (Deworming record : records) {
            if (record.getType() != type) {
                continue;
            }

            if (latest == null || toComparableDate(record).isAfter(toComparableDate(latest))) {
                latest = record;
            }
        }

        return latest;
    }

    private LocalDate toComparableDate(Deworming deworming) {
        return deworming.getExpirationDate() != null ? deworming.getExpirationDate() : deworming.getAdministrationDate();
    }

    @Override
    public void deleteById(Long id) {
        Dog dog = getDogById(id);
        String photoPath = dog.getPhotoPath();

        dogRepository.delete(dog);
        schedulePhotoDeletion(photoPath);
    }

    @Override
    public DogViewDto updateForUser(Long id, String name, String breed, String birthDate, String microchip,
                                    MultipartFile photo) {
        Dog dog = getDogById(id);
        String previousPhotoPath = dog.getPhotoPath();
        String newPhotoPath = null;

        if (name != null && !name.isBlank()) dog.setName(name);
        if (breed != null && !breed.isBlank()) dog.setBreed(breed);
        if (birthDate != null && !birthDate.isBlank()) dog.setBirthDate(parseBirthDate(birthDate));
        if (microchip != null && !microchip.isBlank()) dog.setMicrochip(microchip);
        if (photo != null && !photo.isEmpty()) {
            newPhotoPath = storePhoto(photo);
            dog.setPhotoPath(newPhotoPath);
        }

        try {
            dogRepository.save(dog);
            if (newPhotoPath != null && previousPhotoPath != null && !previousPhotoPath.equals(newPhotoPath)) {
                schedulePhotoDeletion(previousPhotoPath);
            }
            return getViewById(id);
        } catch (RuntimeException ex) {
            deleteStoredPhotoIfExists(newPhotoPath);
            throw ex;
        }
    }

    private Dog getDogById(Long id) {
        return dogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found"));
    }

    private String getAuthenticatedUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }

    private LocalDate parseBirthDate(String birthDate) {
        try {
            return LocalDate.parse(birthDate);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid birthDate format", e);
        }
    }

    private String storePhoto(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            return null;
        }
        String storedFileName = UUID.randomUUID() + "-" + sanitizeFilename(photo.getOriginalFilename());
        ImageKitStorageService.StoredImage storedImage =
                imageKitStorageService.uploadDogPhoto(photo, storedFileName);
        return serializeImageKitPhoto(storedImage);
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "photo";
        }
        return Paths.get(originalFilename).getFileName().toString();
    }

    private Path resolvePhotoPath(Dog dog) {
        if (dog.getPhotoPath() == null || dog.getPhotoPath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
        }

        for (Path candidate : buildPhotoPathCandidates(dog.getPhotoPath())) {
            if (Files.exists(candidate) && Files.isReadable(candidate) && !Files.isDirectory(candidate)) {
                return candidate;
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
    }

    private Set<Path> buildPhotoPathCandidates(String storedPhotoPath) {
        String normalizedPhotoPath = storedPhotoPath.trim();
        Path rawPath = Paths.get(normalizedPhotoPath).normalize();
        Path uploadDirAbsolute = DOG_UPLOAD_DIR.toAbsolutePath().normalize();
        Set<Path> candidates = new LinkedHashSet<>();

        candidates.add(rawPath);
        candidates.add(rawPath.toAbsolutePath().normalize());

        if (!rawPath.isAbsolute()) {
            candidates.add(DOG_UPLOAD_DIR.resolve(rawPath).normalize());
            candidates.add(uploadDirAbsolute.resolve(rawPath).normalize());
        }

        String fileName = rawPath.getFileName() != null ? rawPath.getFileName().toString() : normalizedPhotoPath;
        candidates.add(DOG_UPLOAD_DIR.resolve(fileName).normalize());
        candidates.add(uploadDirAbsolute.resolve(fileName).normalize());

        String sanitizedRelativePath = normalizedPhotoPath
                .replace('\\', '/')
                .replaceFirst("^/", "")
                .replaceFirst("^uploads/dogs/", "")
                .replaceFirst("^dogs/", "");
        if (!sanitizedRelativePath.isBlank()) {
            candidates.add(DOG_UPLOAD_DIR.resolve(sanitizedRelativePath).normalize());
            candidates.add(uploadDirAbsolute.resolve(sanitizedRelativePath).normalize());
        }

        return candidates;
    }

    private void schedulePhotoDeletion(String storedPhotoPath) {
        if (storedPhotoPath == null || storedPhotoPath.isBlank()) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteStoredPhotoIfExists(storedPhotoPath);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteStoredPhotoIfExists(storedPhotoPath);
            }
        });
    }

    private void deleteStoredPhotoIfExists(String storedPhotoPath) {
        if (storedPhotoPath == null || storedPhotoPath.isBlank()) {
            return;
        }

        ImageKitPhotoReference remotePhoto = parseImageKitPhoto(storedPhotoPath);
        if (remotePhoto != null) {
            imageKitStorageService.deleteByFileId(remotePhoto.fileId());
            return;
        }

        for (Path candidate : buildPhotoPathCandidates(storedPhotoPath)) {
            try {
                Files.deleteIfExists(candidate);
                break;
            } catch (IOException ignored) {
                // Si no se puede borrar el archivo, no bloqueamos la operacion principal.
            }
        }
    }

    private String serializeImageKitPhoto(ImageKitStorageService.StoredImage storedImage) {
        return IMAGEKIT_PREFIX + storedImage.fileId() + "|" + storedImage.url();
    }

    private String extractImageKitUrl(String storedPhotoPath) {
        ImageKitPhotoReference remotePhoto = parseImageKitPhoto(storedPhotoPath);
        return remotePhoto != null ? remotePhoto.url() : null;
    }

    private ImageKitPhotoReference parseImageKitPhoto(String storedPhotoPath) {
        if (storedPhotoPath == null || !storedPhotoPath.startsWith(IMAGEKIT_PREFIX)) {
            return null;
        }

        String[] parts = storedPhotoPath.split("\\|", 3);
        if (parts.length < 3 || parts[1].isBlank() || parts[2].isBlank()) {
            return null;
        }

        return new ImageKitPhotoReference(parts[1], parts[2]);
    }

    private MediaType guessMediaTypeFromUrl(String url) {
        String normalizedUrl = url.toLowerCase();
        if (normalizedUrl.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (normalizedUrl.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        if (normalizedUrl.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        return MediaType.IMAGE_JPEG;
    }

    private record ImageKitPhotoReference(String fileId, String url) {
    }
}
