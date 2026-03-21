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
import com.project.pets.domain.dto.deworming.DewormingViewDto;
import com.project.pets.domain.dto.vaccine.VaccineDogDto;
import com.project.pets.domain.dto.vaccine.VaccineDogViewDto;
import com.project.pets.domain.dto.vaccine.VaccineListDto;
import com.project.pets.repository.DewormingRepository;
import com.project.pets.repository.DogRepository;
import com.project.pets.repository.DogVaccineRepository;
import com.project.pets.repository.OwnerRepository;
import com.project.pets.repository.UserRepository;
import com.project.pets.repository.VaccineRepository;
import com.project.pets.service.DogService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class DogServiceImpl implements DogService {

    private static final Path DOG_UPLOAD_DIR = Paths.get("uploads", "dogs");

    private final DogRepository dogRepository;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final DogVaccineRepository dogVaccineRepository;
    private final VaccineRepository vaccineRepository;
    private final DewormingRepository dewormingRepository;

    public DogServiceImpl(DogRepository dogRepository, OwnerRepository ownerRepository,
                          UserRepository userRepository, DogVaccineRepository dogVaccineRepository,
                          VaccineRepository vaccineRepository, DewormingRepository dewormingRepository) {
        this.dogRepository = dogRepository;
        this.ownerRepository = ownerRepository;
        this.userRepository = userRepository;
        this.dogVaccineRepository = dogVaccineRepository;
        this.vaccineRepository = vaccineRepository;
        this.dewormingRepository = dewormingRepository;
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

        return saveForUser(dogDto, photoPath, username);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getPhotoResource(Long id) {
        Dog dog = getDogById(id);
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
        dto.setName(dog.getName());
        dto.setBreed(dog.getBreed());
        dto.setBirthDate(dog.getBirthDate());
        dto.setMicrochip(dog.getMicrochip());
        dto.setOwnerName(dog.getOwner().getName());
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

    @Override
    public void deleteById(Long id) {
        dogRepository.deleteById(id);
    }

    @Override
    public DogViewDto updateForUser(Long id, String name, String breed, String birthDate, String microchip,
                                    MultipartFile photo) {
        Dog dog = getDogById(id);

        if (name != null && !name.isBlank()) dog.setName(name);
        if (breed != null && !breed.isBlank()) dog.setBreed(breed);
        if (birthDate != null && !birthDate.isBlank()) dog.setBirthDate(parseBirthDate(birthDate));
        if (microchip != null && !microchip.isBlank()) dog.setMicrochip(microchip);
        if (photo != null && !photo.isEmpty()) dog.setPhotoPath(storePhoto(photo));

        dogRepository.save(dog);
        return getViewById(id);
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

        try {
            Files.createDirectories(DOG_UPLOAD_DIR);
            String storedFileName = UUID.randomUUID() + "-" + sanitizeFilename(photo.getOriginalFilename());
            Path targetPath = DOG_UPLOAD_DIR.resolve(storedFileName).normalize();
            Files.copy(photo.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toString();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store photo", e);
        }
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
}
