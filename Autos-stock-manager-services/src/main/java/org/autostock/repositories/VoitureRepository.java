package org.autostock.repositories;

import org.autostock.enums.StatutVoiture;
import org.autostock.models.Voiture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoitureRepository extends JpaRepository<Voiture, Long> {

    // Par statut : EN_STOCK, VENDUE, RESERVEE...
    List<Voiture> findByStatut(StatutVoiture statut);

    // Toutes les voitures en stock d'une marque donnée
//    List<Voiture> findByModele_Marque_NomIgnoreCaseAndStatut(String nomMarque, StatutVoiture statut);

    // Par VIN (unique)
    Optional<Voiture> findByVin(String vin);

//    boolean existsByVin(String vin);
//
//    // Voitures entrées en stock dans une période donnée (utile pour reporting)
//    List<Voiture> findByDateEntreeStockBetween(LocalDateTime debut, LocalDateTime fin);

}
