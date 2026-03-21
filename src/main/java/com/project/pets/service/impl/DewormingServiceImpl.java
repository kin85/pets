package com.project.pets.service.impl;

import com.project.pets.repository.DewormingRepository;
import com.project.pets.service.DewormingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class DewormingServiceImpl implements DewormingService {

    private final DewormingRepository dewormingRepository;

    public DewormingServiceImpl(DewormingRepository dewormingRepository) {
        this.dewormingRepository = dewormingRepository;
    }

    @Override
    public void delete(Long dewormingId) {
        if (!dewormingRepository.existsById(dewormingId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Deworming not found");
        }
        dewormingRepository.deleteById(dewormingId);
    }
}

