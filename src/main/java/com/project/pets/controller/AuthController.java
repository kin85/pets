package com.project.pets.controller;

import com.project.pets.domain.dto.UserRegisterDto;
import com.project.pets.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token, Long ownerId, List<String> roles) {}

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/register")
    public void register(@RequestBody UserRegisterDto dto) {
        authService.save(dto);
    }


}
