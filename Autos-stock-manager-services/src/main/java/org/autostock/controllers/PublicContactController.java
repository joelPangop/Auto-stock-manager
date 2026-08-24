package org.autostock.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.autostock.dtos.client.ContactDto;
import org.autostock.services.MailDispatcher;
import org.autostock.services.MailTemplates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/contact")
@Slf4j
public class PublicContactController {

    @Autowired
    private MailDispatcher mailDispatcher;

    @Value("${autostock.ses.from:joelpangop@gmail.com}")
    private String adminEmail;

    @PostMapping
    public ResponseEntity<Map<String, String>> contact(@Valid @RequestBody ContactDto dto) {
        log.info("[Contact] Message reçu de {} <{}>", dto.nom(), dto.email());

        boolean envoye = mailDispatcher.send(
                adminEmail,
                "Contact Ted Auto : " + dto.sujet(),
                MailTemplates.contact(dto.nom(), dto.email(), dto.telephone(), dto.sujet(), dto.message())
        );

        // Le contenu du message est tracé si aucun canal n'a fonctionné, pour ne
        // pas perdre une demande client derrière un accusé de réception optimiste.
        if (!envoye) {
            log.error("[Contact] Message NON transmis — de {} <{}> tel={} sujet={} : {}",
                    dto.nom(), dto.email(), dto.telephone(), dto.sujet(), dto.message());
        }

        return ResponseEntity.ok(Map.of("message", "Votre message a bien été envoyé."));
    }
}
