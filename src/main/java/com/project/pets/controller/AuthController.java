package com.project.pets.controller;

import com.project.pets.domain.dto.UserRegisterDto;
import com.project.pets.service.AuthService;
import org.springframework.http.HttpStatus;
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
    public record TokenRequest(String token) {}
    public record EmailRequest(String email) {}
    public record PasswordResetRequest(String token, String password) {}

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/register")
    public void register(@RequestBody UserRegisterDto dto) {
        authService.save(dto);
    }

    @PostMapping("/confirm-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmEmail(@RequestBody TokenRequest req) {
        authService.confirmEmail(req.token());
    }

    @PostMapping("/confirm-email/resend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendConfirmationEmail(@RequestBody EmailRequest req) {
        authService.resendConfirmationEmail(req.email());
    }

    @PostMapping("/password-reset/request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPasswordReset(@RequestBody EmailRequest req) {
        authService.requestPasswordReset(req.email());
    }

    @PostMapping("/password-reset/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody PasswordResetRequest req) {
        authService.resetPassword(req.token(), req.password());
    }
}
