package com.project.pets.service.impl;

import com.project.pets.domain.Dog;
import com.project.pets.domain.Owner;
import com.project.pets.domain.User;
import com.project.pets.domain.dto.DogHomeDto;
import com.project.pets.domain.dto.OwnerDto;
import com.project.pets.domain.dto.OwnerHomeDto;
import com.project.pets.domain.dto.OwnerProfileDto;
import com.project.pets.domain.dto.OwnerProfileUpdateDto;
import com.project.pets.repository.DogRepository;
import com.project.pets.repository.OwnerRepository;
import com.project.pets.repository.UserRepository;
import com.project.pets.service.OwnerService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class OwnerServiceImpl implements OwnerService {

    private final OwnerRepository repository;

    private final DogRepository dogRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OwnerServiceImpl(OwnerRepository repository,
                            DogRepository dogRepository,
                            UserRepository userRepository,
                            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.dogRepository = dogRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OwnerDto getById(Long id) {
        Owner owner = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Propietario no encontrado"));
        return mapToDto(owner);
    }

    @Override
    public Long save(OwnerDto ownerDto) {
        Owner owner = mapToEntity(ownerDto);
        repository.save(owner);
        return owner.getId();
    }

    @Override
    public OwnerHomeDto getOwnerHome(Long id) {
        Owner owner = repository.findById(id).orElseThrow();
        return mapToOwnerHomeDto(owner);
    }

    @Override
    public OwnerHomeDto getAuthenticatedOwnerHome(Authentication authentication) {
        String username = getAuthenticatedUsername(authentication);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        Owner owner = repository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Propietario no encontrado"));
        return mapToOwnerHomeDto(owner);
    }

    @Override
    public OwnerProfileDto getAuthenticatedProfile(Authentication authentication) {
        Owner owner = getAuthenticatedOwner(authentication);
        return mapToProfileDto(owner);
    }

    @Override
    @Transactional
    public OwnerProfileDto updateAuthenticatedProfile(OwnerProfileUpdateDto profileUpdateDto,
                                                      Authentication authentication) {
        Owner owner = getAuthenticatedOwner(authentication);
        User user = owner.getUser();

        String email = requireText(profileUpdateDto.getEmail(), "El email es obligatorio");
        String name = requireText(profileUpdateDto.getName(), "El nombre es obligatorio");
        String address = requireText(profileUpdateDto.getAddress(), "La direccion es obligatoria");
        String phone = requireText(profileUpdateDto.getPhone(), "El telefono es obligatorio");

        userRepository.findByEmail(email)
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new ResponseStatusException(CONFLICT, "El email ya esta en uso");
                });

        user.setEmail(email);
        owner.setName(name);
        owner.setAddress(address);
        owner.setPhone(phone);

        if (profileUpdateDto.getPassword() != null && !profileUpdateDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(profileUpdateDto.getPassword().trim()));
        }

        userRepository.save(user);
        repository.save(owner);
        return mapToProfileDto(owner);
    }

    private Owner mapToEntity(OwnerDto ownerDto) {
        Owner owner = new Owner();
        owner.setName(ownerDto.getName());
        owner.setAddress(ownerDto.getAddress());
        owner.setPhone(ownerDto.getPhone());
        return owner;
    }

    private OwnerDto mapToDto(Owner owner) {
        OwnerDto dto = new OwnerDto();
        dto.setName(owner.getName());
        dto.setAddress(owner.getAddress());
        dto.setPhone(owner.getPhone());
        return dto;
    }

    private OwnerHomeDto mapToOwnerHomeDto(Owner owner){
        OwnerHomeDto dto = new OwnerHomeDto();
        dto.setName(owner.getName());
        dto.setDogs(obtaindogsDtoList(owner.getId()));
        return dto;
    }

    private OwnerProfileDto mapToProfileDto(Owner owner) {
        OwnerProfileDto dto = new OwnerProfileDto();
        dto.setUsername(owner.getUser().getUsername());
        dto.setEmail(owner.getUser().getEmail());
        dto.setName(owner.getName());
        dto.setAddress(owner.getAddress());
        dto.setPhone(owner.getPhone());
        return dto;
    }

    private List<DogHomeDto> obtaindogsDtoList(Long ownerId){
        List<Dog> dogList = dogRepository.findAllByOwnerId(ownerId);
        List<DogHomeDto> dogHomeDtoList = new ArrayList<>();

        for (Dog dog : dogList){
            DogHomeDto dogHomeDto = mapToDogHomeDto(dog);
            dogHomeDtoList.add(dogHomeDto);
        }

        return dogHomeDtoList;
    }

    private DogHomeDto mapToDogHomeDto(Dog dog){
        DogHomeDto dto = new DogHomeDto();
        dto.setName(dog.getName());
        dto.setId(dog.getId());
        dto.setHasPhoto(dog.getPhotoPath() != null && !dog.getPhotoPath().isBlank());
        dto.setPhotoUrl(extractImageKitUrl(dog.getPhotoPath()));
        return dto;
    }

    private String extractImageKitUrl(String photoPath) {
        if (photoPath == null || !photoPath.startsWith("imagekit|")) {
            return null;
        }

        String[] parts = photoPath.split("\\|", 3);
        if (parts.length < 3 || parts[2].isBlank()) {
            return null;
        }

        return parts[2];
    }

    private String getAuthenticatedUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(NOT_FOUND, "Usuario autenticado no disponible");
        }
        return authentication.getName();
    }

    private Owner getAuthenticatedOwner(Authentication authentication) {
        String username = getAuthenticatedUsername(authentication);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        return repository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Propietario no encontrado"));
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, message);
        }
        return value.trim();
    }
}
