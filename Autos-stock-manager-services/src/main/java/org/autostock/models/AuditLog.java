package org.autostock.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;          // PAIEMENT_CREATED, PAIEMENT_DELETED...
    private Long actorUserId;        // qui a fait l’action
    private Long entityId;           // ex: paiementId
    private String entityType;       // "PAIEMENT"
    private String details;          // texte libre

    private LocalDateTime occurredAt;
}
