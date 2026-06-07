package org.autostock.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IReceiptPdfService {

    byte[] generatePaiementReceipt(
            Long paiementId,
            String voitureLabel,
            BigDecimal montant,
            String methode,
            LocalDateTime date
    );
}
