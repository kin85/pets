package com.project.pets.service.impl;

import com.project.pets.controller.AuthController;
import com.project.pets.domain.Owner;
import com.project.pets.domain.Role;
import com.project.pets.domain.User;
import com.project.pets.domain.dto.UserRegisterDto;
import com.project.pets.repository.OwnerRepository;
import com.project.pets.repository.RoleRepository;
import com.project.pets.repository.UserRepository;
import com.project.pets.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.project.pets.security.JwtService;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           UserRepository userRepository,
                           OwnerRepository ownerRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Long save(UserRegisterDto userRegisterDto) {

        if (userRepository.findByUsername(userRegisterDto.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.findByEmail(userRegisterDto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Role USER not found"));

        User user = new User();
        user.setUsername(userRegisterDto.getUsername());
        user.setEmail(userRegisterDto.getEmail());
        user.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
        user.getRoles().add(userRole);
        user = userRepository.save(user);

        Owner owner = new Owner();
        owner.setUser(user);
        owner.setName(userRegisterDto.getName());
        owner.setAddress(userRegisterDto.getAddress());
        owner.setPhone(userRegisterDto.getPhone());
        ownerRepository.save(owner);

        return user.getId();
    }

    @Override
    public AuthController.LoginResponse login(AuthController.LoginRequest req) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        var springUser = (UserDetails) auth.getPrincipal();
        String username = springUser.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Owner owner = ownerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Owner not found"));

        String token = jwtService.generateToken(springUser);

        return new AuthController.LoginResponse(token, owner.getId());
    }
}
