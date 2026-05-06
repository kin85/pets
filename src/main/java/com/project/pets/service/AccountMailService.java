package com.project.pets.service;

import com.project.pets.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class AccountMailService {

    private final JavaMailSender mailSender;
    private final String frontendBaseUrl;
    private final String from;

    public AccountMailService(
            JavaMailSender mailSender,
            @Value("${app.frontend.base-url}") String frontendBaseUrl,
            @Value("${app.mail.from}") String from
    ) {
        this.mailSender = mailSender;
        this.frontendBaseUrl = frontendBaseUrl;
        this.from = from;
    }

    public void sendEmailConfirmation(User user, String token) {
        send(
                user.getEmail(),
                "Confirma tu cuenta de Appets",
                """
                Hola %s,

                Para activar tu cuenta de Appets, confirma tu correo en este enlace:
                %s

                Si no has creado esta cuenta, puedes ignorar este mensaje.
                """.formatted(defaultName(user), buildUrl("/confirm-email", token))
        );
    }

    public void sendPasswordReset(User user, String token) {
        send(
                user.getEmail(),
                "Restablece tu contraseña de Appets",
                """
                Hola %s,

                Puedes restablecer tu contraseña desde este enlace:
                %s

                Si no has solicitado este cambio, puedes ignorar este mensaje.
                """.formatted(defaultName(user), buildUrl("/reset-password", token))
        );
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private String buildUrl(String path, String token) {
        String normalizedBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        return normalizedBaseUrl + path + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String defaultName(User user) {
        return user.getUsername() == null || user.getUsername().isBlank() ? "usuario" : user.getUsername();
    }
}
