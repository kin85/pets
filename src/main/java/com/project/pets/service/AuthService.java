package com.project.pets.service;

import com.project.pets.controller.AuthController;
import com.project.pets.domain.dto.UserRegisterDto;

public interface AuthService {

    Long save(UserRegisterDto userRegisterDto);

    AuthController.LoginResponse login(AuthController.LoginRequest req);

    void confirmEmail(String token);

    void resendConfirmationEmail(String email);

    void requestPasswordReset(String email);

    void resetPassword(String token, String password);
}
