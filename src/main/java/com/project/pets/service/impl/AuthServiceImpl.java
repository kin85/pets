package com.project.pets.service.impl;

import com.project.pets.controller.AuthController;
import com.project.pets.domain.Owner;
import com.project.pets.domain.Role;
import com.project.pets.domain.User;
import com.project.pets.domain.UserToken;
import com.project.pets.domain.dto.UserRegisterDto;
import com.project.pets.domain.enums.UserTokenType;
import com.project.pets.repository.OwnerRepository;
import com.project.pets.repository.RoleRepository;
import com.project.pets.repository.UserRepository;
import com.project.pets.repository.UserTokenRepository;
import com.project.pets.service.AccountMailService;
import com.project.pets.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.project.pets.security.JwtService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int EMAIL_CONFIRMATION_EXPIRATION_HOURS = 24;
    private static final int PASSWORD_RESET_EXPIRATION_HOURS = 1;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final RoleRepository roleRepository;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMailService accountMailService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           UserRepository userRepository,
                           OwnerRepository ownerRepository,
                           RoleRepository roleRepository,
                           UserTokenRepository userTokenRepository,
                           AccountMailService accountMailService,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.roleRepository = roleRepository;
        this.userTokenRepository = userTokenRepository;
        this.accountMailService = accountMailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Long save(UserRegisterDto userRegisterDto) {
        validatePassword(userRegisterDto.getPassword());

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
        user.setEmailVerified(false);
        user.getRoles().add(userRole);
        user = userRepository.save(user);

        Owner owner = new Owner();
        owner.setUser(user);
        owner.setName(userRegisterDto.getName());
        owner.setAddress(userRegisterDto.getAddress());
        owner.setPhone(userRegisterDto.getPhone());
        ownerRepository.save(owner);
        issueEmailConfirmation(user);

        return user.getId();
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 8 caracteres");
        }
    }

    @Override
    public AuthController.LoginResponse login(AuthController.LoginRequest req) {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );

            var springUser = (UserDetails) auth.getPrincipal();
            String username = springUser.getUsername();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

            Optional<Owner> owner = ownerRepository.findByUserId(user.getId());

            String token = jwtService.generateToken(springUser);
            List<String> roles = springUser.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .toList();

            return new AuthController.LoginResponse(token, owner.map(Owner::getId).orElse(null), roles);
        } catch (DisabledException ex) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Debes confirmar tu correo electronico antes de iniciar sesion"
            );
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo iniciar sesion");
        }
    }

    @Override
    public void confirmEmail(String token) {
        UserToken userToken = getValidToken(token, UserTokenType.EMAIL_CONFIRMATION,
                "El enlace de confirmacion no es valido");

        User user = userToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        userTokenRepository.deleteByUser_IdAndType(user.getId(), UserTokenType.EMAIL_CONFIRMATION);
    }

    @Override
    public void resendConfirmationEmail(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email.trim())
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::issueEmailConfirmation);
    }

    @Override
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email.trim())
                .filter(User::isEmailVerified)
                .ifPresent(this::issuePasswordReset);
    }

    @Override
    public void resetPassword(String token, String password) {
        validatePassword(password);

        UserToken userToken = getValidToken(token, UserTokenType.PASSWORD_RESET,
                "El enlace para restablecer la contraseña no es valido");

        User user = userToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        userTokenRepository.deleteByUser_IdAndType(user.getId(), UserTokenType.PASSWORD_RESET);
    }

    private void issueEmailConfirmation(User user) {
        UserToken userToken = createToken(user, UserTokenType.EMAIL_CONFIRMATION, EMAIL_CONFIRMATION_EXPIRATION_HOURS);
        sendEmailConfirmation(user, userToken.getToken());
    }

    private void issuePasswordReset(User user) {
        UserToken userToken = createToken(user, UserTokenType.PASSWORD_RESET, PASSWORD_RESET_EXPIRATION_HOURS);
        sendPasswordReset(user, userToken.getToken());
    }

    private UserToken createToken(User user, UserTokenType type, int expirationHours) {
        userTokenRepository.deleteByUser_IdAndType(user.getId(), type);

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setType(type);
        userToken.setToken(generateToken());
        userToken.setCreatedAt(LocalDateTime.now());
        userToken.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));
        return userTokenRepository.save(userToken);
    }

    private UserToken getValidToken(String token, UserTokenType type, String invalidMessage) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidMessage);
        }

        UserToken userToken = userTokenRepository.findByTokenAndType(token.trim(), type)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidMessage));

        if (userToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            userTokenRepository.delete(userToken);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace ha caducado");
        }

        return userToken;
    }

    private void sendEmailConfirmation(User user, String token) {
        try {
            accountMailService.sendEmailConfirmation(user, token);
        } catch (MailException ex) {
            log.error("Error enviando correo de confirmacion a {}", user.getEmail(), ex);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo enviar el correo de confirmacion",
                    ex
            );
        }
    }

    private void sendPasswordReset(User user, String token) {
        try {
            accountMailService.sendPasswordReset(user, token);
        } catch (MailException ex) {
            log.error("Error enviando correo para restablecer contraseña a {}", user.getEmail(), ex);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo enviar el correo para restablecer la contraseña",
                    ex
            );
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
