package org.autostock.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.autostock.dtos.client.ReservationCreateDto;
import org.autostock.dtos.client.ReservationDto;
import org.autostock.services.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestion des réservations de visites/essais par les clients du portail.
 * Chemins : /api/client/reservations/**
 */
@RestController
@RequestMapping("/api/client/reservations")
@RequiredArgsConstructor
public class ClientReservationController {

    private final ReservationService service;

    @PostMapping
    public ResponseEntity<ReservationDto> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReservationCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(userDetails.getUsername(), dto));
    }

    @GetMapping
    public ResponseEntity<List<ReservationDto>> list(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(service.listByClient(userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        service.cancel(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
