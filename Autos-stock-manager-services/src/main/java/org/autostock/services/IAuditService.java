package org.autostock.services;


import java.time.LocalDateTime;


public interface IAuditService {
    public void logPaiementCreated(
            Long userId,
            Long paiementId,
            Long venteId,
            Object montant,
            String methode,
            LocalDateTime when
    );
}
