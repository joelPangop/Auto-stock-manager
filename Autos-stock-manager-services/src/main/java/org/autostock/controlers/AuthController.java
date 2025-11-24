package org.autostock.controlers;

import jakarta.validation.Valid;
import org.autostock.dtos.auth.AuthResponse;
import org.autostock.dtos.auth.LoginRequest;
import org.autostock.dtos.auth.RegisterRequest;
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
}
