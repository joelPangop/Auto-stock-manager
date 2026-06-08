package org.autostock.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.autostock.dtos.client.*;
import org.autostock.services.CompteClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Authentification des clients du portail Ted Auto.
 * Chemins : /api/client/auth/**
 */
@RestController
@RequestMapping("/api/client/auth")
@RequiredArgsConstructor
public class ClientAuthController {

    private final CompteClientService service;

    @PostMapping("/register")
    public ResponseEntity<ClientAuthResponse> register(@Valid @RequestBody CompteClientRegisterDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<ClientAuthResponse> login(@Valid @RequestBody CompteClientLoginDto dto) {
        return ResponseEntity.ok(service.login(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<CompteClientDto> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(service.getProfile(userDetails.getUsername()));
    }

    @PutMapping("/me")
    public ResponseEntity<CompteClientDto> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CompteClientUpdateDto dto) {
        return ResponseEntity.ok(service.updateProfile(userDetails.getUsername(), dto));
    }
}
