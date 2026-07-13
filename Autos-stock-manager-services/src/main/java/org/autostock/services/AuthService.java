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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.logging.Logger;

@Service
public class AuthService {

    private static final Logger log = Logger.getLogger(AuthService.class.getName());

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordResetTokenRepository tokenRepo;

    @Autowired
    private EmailService emailService;

    @Autowired(required = false)
    private SesEmailService sesEmailService;

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

        if (u.getPasswordExpiresAt() != null && LocalDateTime.now().isAfter(u.getPasswordExpiresAt())) {
            u.setAccountLocked(true);
            repo.save(u);
            throw new IllegalStateException("Mot de passe temporaire expiré. Contactez un Super Admin pour régénérer votre accès.");
        }

        if (u.isAccountLocked()) {
            throw new IllegalStateException("Compte verrouillé. Contactez un Super Admin.");
        }

        return buildAuthResponse(u);
    }

    @Transactional
    public void createUserByAdmin(AdminCreateUserRequest req, String creatorRole) {
        if (repo.findByEmail(req.email()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        Role targetRole = (req.role() != null && !req.role().isBlank())
                ? Role.valueOf(req.role().toUpperCase())
                : Role.USER;

        if (targetRole == Role.SUPER_ADMIN && !creatorRole.equals("SUPER_ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Seul un SUPER_ADMIN peut créer un SUPER_ADMIN");
        }

        String plainPassword = generateSecurePassword();

        var u = User.builder()
                .nom(req.nom())
                .email(req.email())
                .motDePasseHash(encoder.encode(plainPassword))
                .phoneNumber(req.phoneNumber())
                .role(targetRole)
                .passwordExpiresAt(LocalDateTime.now().plusDays(14))
                .accountLocked(false)
                .build();
        repo.save(u);

        sendWelcomeEmail(req.email(), req.nom(), plainPassword);
    }

    @Transactional
    public void regeneratePassword(Long userId) {
        var u = repo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));

        String plainPassword = generateSecurePassword();
        u.setMotDePasseHash(encoder.encode(plainPassword));
        u.setPasswordExpiresAt(LocalDateTime.now().plusDays(14));
        u.setAccountLocked(false);
        repo.save(u);

        sendWelcomeEmail(u.getEmail(), u.getNom(), plainPassword);
    }

    private void sendWelcomeEmail(String to, String nom, String password) {
        try {
            if (sesEmailService != null) {
                sesEmailService.sendWelcomePassword(to, nom, password);
            } else {
                emailService.sendWelcomePassword(to, nom, password);
            }
        } catch (Exception e) {
            log.warning("Email non envoyé à " + to + " : " + e.getMessage());
        }
    }

    private String generateSecurePassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%&*";
        String all = upper + lower + digits + special;

        SecureRandom rng = new SecureRandom();
        char[] chars = new char[10];
        chars[0] = upper.charAt(rng.nextInt(upper.length()));
        chars[1] = digits.charAt(rng.nextInt(digits.length()));
        chars[2] = special.charAt(rng.nextInt(special.length()));
        for (int i = 3; i < 10; i++) {
            chars[i] = all.charAt(rng.nextInt(all.length()));
        }
        for (int i = 9; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            char tmp = chars[i]; chars[i] = chars[j]; chars[j] = tmp;
        }
        return new String(chars);
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
        u.setPasswordExpiresAt(null);
        u.setAccountLocked(false);
        repo.save(u);

        token.setUsed(true);
        tokenRepo.save(token);
    }

    private User findByIdentifier(String identifier) {
        String trimmed = identifier.trim();
        if (trimmed.contains("@")) {
            return repo.findByEmailIgnoreCase(trimmed)
                    .orElseThrow(() -> new EntityNotFoundException("Aucun compte trouvé avec cet email"));
        }
        String normalized = trimmed.replaceAll("\\s+", "");
        return repo.findByPhoneNumber(normalized)
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
