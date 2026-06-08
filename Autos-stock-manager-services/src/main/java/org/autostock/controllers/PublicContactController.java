package org.autostock.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.autostock.dtos.client.ContactDto;
import org.autostock.services.SesEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/contact")
@Slf4j
public class PublicContactController {

    @Autowired(required = false)
    private SesEmailService sesEmailService;

    @Value("${autostock.ses.from:joelpangop@gmail.com}")
    private String adminEmail;

    @PostMapping
    public ResponseEntity<Map<String, String>> contact(@Valid @RequestBody ContactDto dto) {
        log.info("[Contact] Message reçu de {} <{}>", dto.nom(), dto.email());
        if (sesEmailService != null) {
            try {
                sesEmailService.sendContactMessage(
                        adminEmail,
                        dto.nom(),
                        dto.email(),
                        dto.telephone(),
                        dto.sujet(),
                        dto.message()
                );
            } catch (Exception e) {
                log.error("[Contact] Erreur envoi SES : {}", e.getMessage());
            }
        } else {
            log.warn("[Contact] SesEmailService non disponible — email non envoyé");
        }
        return ResponseEntity.ok(Map.of("message", "Votre message a bien été envoyé."));
    }
}
