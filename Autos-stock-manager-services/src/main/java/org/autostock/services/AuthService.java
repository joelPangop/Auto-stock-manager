package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.dtos.auth.*;
import org.autostock.enums.DeliveryMethod;
import org.autostock.enums.Role;
import org.autostock.models.PasswordResetToken;
import org.autostock.models.User;
import org.autostock.repositories.PasswordResetTokenRepository;
import org.autostock.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordResetTokenRepository tokenRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthService(PasswordEncoder encoder,
                       AuthenticationManager authManager, JwtService jwt) {
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest req) {
        if (repo.findByEmail(req.email()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        var u = User.builder()
                .nom(req.nom())
                .email(req.email())
                .motDePasseHash(encoder.encode(req.password()))
                .phoneNumber(req.phoneNumber())
                .role(Role.USER)
                .build();
        repo.save(u);
        return buildAuthResponse(u);
    }

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        var u = repo.findByEmail(req.email()).orElseThrow();
        return buildAuthResponse(u);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwt.isTokenValid(refreshToken) || !jwt.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token invalide");
        }
        var email = jwt.extractUsername(refreshToken);
        var u = repo.findByEmail(email).orElseThrow();
        return buildAuthResponse(u, refreshToken);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        var u = findByIdentifier(req.identifier());

        if (req.deliveryMethod() == DeliveryMethod.SMS) {
            if (u.getPhoneNumber() == null || u.getPhoneNumber().isBlank()) {
                throw new IllegalStateException("Aucun numéro de téléphone associé à ce compte");
            }
        } else {
            if (u.getEmail() == null || u.getEmail().isBlank()) {
                throw new IllegalStateException("Aucune adresse email associée à ce compte");
            }
        }

        tokenRepo.deleteByUser(u);

        String code = String.format("%06d", new Random().nextInt(1_000_000));

        tokenRepo.save(PasswordResetToken.builder()
                .user(u)
                .code(code)
                .deliveryMethod(req.deliveryMethod())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build());

        if (req.deliveryMethod() == DeliveryMethod.EMAIL) {
            emailService.sendPasswordResetCode(u.getEmail(), code);
        } else {
            smsService.sendPasswordResetCode(u.getPhoneNumber(), code);
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        var u = findByIdentifier(req.identifier());

        var token = tokenRepo.findByUserAndCodeAndUsedFalseAndExpiresAtAfter(
                u, req.code(), LocalDateTime.now()
        ).orElseThrow(() -> new IllegalArgumentException("Code invalide ou expiré"));

        u.setMotDePasseHash(encoder.encode(req.newPassword()));
        repo.save(u);

        token.setUsed(true);
        tokenRepo.save(token);
    }

    private User findByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return repo.findByEmail(identifier)
                    .orElseThrow(() -> new EntityNotFoundException("Aucun compte trouvé avec cet email"));
        }
        return repo.findByPhoneNumber(identifier)
                .orElseThrow(() -> new EntityNotFoundException("Aucun compte trouvé avec ce numéro de téléphone"));
    }

    private AuthResponse buildAuthResponse(User u) {
        return buildAuthResponse(u, jwt.generateRefreshToken(u));
    }

    private AuthResponse buildAuthResponse(User u, String refreshToken) {
        String access = jwt.generateAccessToken(u);
        return new AuthResponse(
                access,
                refreshToken,
                "Bearer",
                jwt.getAccessExpirationMs() / 1000,
                new AuthResponse.UserView(u.getId(), u.getNom(), u.getEmail(), u.getRole().name())
        );
    }
}
