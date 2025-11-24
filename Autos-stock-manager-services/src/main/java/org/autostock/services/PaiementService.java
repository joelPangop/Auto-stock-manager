package org.autostock.services;

import org.autostock.models.Paiement;

import java.math.BigDecimal;
import java.util.List;

public interface PaiementService extends IService<Paiement, Long> {

    Paiement ajouterPaiement(Long idVente, BigDecimal montant, String methodeValue);

    List<Paiement> paiementsDeLaVente(Long idVente);

    BigDecimal totalPaye(Long idVente);

    BigDecimal resteAPayer(Long idVente);
}
