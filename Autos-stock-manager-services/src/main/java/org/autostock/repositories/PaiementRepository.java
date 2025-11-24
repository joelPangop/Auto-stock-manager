package org.autostock.repositories;
import org.autostock.enums.MethodePaiement;
import org.autostock.models.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    // Tous les paiements d'une vente
//    List<Paiement> findByVente_IdVente(Long idVente);

    // Paiements sur une période (ex: pour la trésorerie)
//    List<Paiement> findByDatePaiementBetween(LocalDateTime debut, LocalDateTime fin);

    // Tous les paiements d'un certain type
    List<Paiement> findByMethode(MethodePaiement methode);

    List<Paiement> findByVente_Id(Long venteId);
}
