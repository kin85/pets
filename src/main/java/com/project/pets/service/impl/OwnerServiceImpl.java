package com.project.pets.service.impl;

import com.project.pets.domain.Dog;
import com.project.pets.domain.Owner;
import com.project.pets.domain.dto.DogHomeDto;
import com.project.pets.domain.dto.OwnerDto;
import com.project.pets.domain.dto.OwnerHomeDto;
import com.project.pets.repository.DogRepository;
import com.project.pets.repository.OwnerRepository;
import com.project.pets.service.OwnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class OwnerServiceImpl implements OwnerService {

    private final OwnerRepository repository;

    private final DogRepository dogRepository;

    public OwnerServiceImpl(OwnerRepository repository, DogRepository dogRepository) {
        this.repository = repository;
        this.dogRepository = dogRepository;
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
        return dto;
    }
}
