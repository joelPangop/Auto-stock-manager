package org.autostock.services;


import lombok.RequiredArgsConstructor;
import org.autostock.models.AuditLog;
import org.autostock.repositories.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService implements IAuditService {

    @Autowired
    private AuditLogRepository repository;

    public void logPaiementCreated(
            Long userId,
            Long paiementId,
            Long venteId,
            Object montant,
            String methode,
            LocalDateTime when
    ) {
        AuditLog log = new AuditLog();
        log.setAction("PAIEMENT_CREATED");
        log.setActorUserId(userId);
        log.setEntityType("PAIEMENT");
        log.setEntityId(paiementId);
        log.setDetails("venteId=" + venteId + ", montant=" + montant + ", methode=" + methode);
        log.setOccurredAt(when);

        repository.save(log);
    }
}
