package org.autostock.services;

import org.autostock.dtos.auth.AuthResponse;
import org.autostock.dtos.auth.LoginRequest;
import org.autostock.dtos.auth.RegisterRequest;
import org.autostock.enums.Role;
import org.autostock.models.User;
import org.autostock.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository repo;

    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthService(PasswordEncoder encoder,
                       AuthenticationManager authManager, JwtService jwt) {
       this.encoder = encoder; this.authManager = authManager; this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest req) {
        if (repo.findByEmail(req.email()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        var u = User.builder()
                .nom(req.nom())
                .email(req.email())
                .motDePasseHash(encoder.encode(req.password()))
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
        return buildAuthResponse(u, refreshToken); // on réutilise le refresh existant
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
                jwt.getAccessExpirationMs()/1000,
                new AuthResponse.UserView(u.getId(), u.getNom(), u.getEmail(), u.getRole().name())
        );
    }
}
