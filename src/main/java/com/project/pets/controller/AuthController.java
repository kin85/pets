package com.project.pets.controller;

import com.project.pets.domain.Owner;
import com.project.pets.domain.User;
import com.project.pets.domain.dto.UserRegisterDto;
import com.project.pets.security.JwtService;
import com.project.pets.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authService = authService;
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token, Long id) {}

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/register")
    public void register(@RequestBody UserRegisterDto dto) {
        authService.save(dto);
    }


}