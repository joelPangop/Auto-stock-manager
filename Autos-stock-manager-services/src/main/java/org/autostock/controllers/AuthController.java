package org.autostock.controllers;

import jakarta.validation.Valid;
import org.autostock.dtos.auth.*;
import org.autostock.repositories.UserRepository;
import org.autostock.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository repo;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refresh = body.get("refreshToken");
        return ResponseEntity.ok(authService.refresh(refresh));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        var u = repo.findByEmail(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(new AuthResponse.UserView(
                u.getId(), u.getNom(), u.getEmail(), u.getRole().name()
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req,
                                               @AuthenticationPrincipal UserDetails principal) {
        // /api/auth/** est public : sans ce garde-fou, l'endpoint serait
        // atteignable sans jeton. Le SecurityConfig l'exige aussi, la double
        // verification evite qu'un remaniement des regles ouvre une breche.
        if (principal == null) return ResponseEntity.status(401).build();
        authService.changePassword(principal.getUsername(), req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok().build();
    }
}
