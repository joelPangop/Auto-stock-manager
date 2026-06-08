package org.autostock.repositories;

import org.autostock.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Tous les documents liés à une voiture
//    List<Document> findByVoiture_Id(Long idVoiture);

    // Filtrer par type (Facture, Immatriculation, etc.)
    List<Document> findByTypeAndVoiture_Id(String type, Long voitureId) ;

    List<Document> findByVoiture_Id(Long idVoiture);

    /** Retourne la photo principale d'une voiture, s'il y en a une. */
    java.util.Optional<Document> findFirstByVoiture_IdAndPrincipaleTrue(Long voitureId);

    /** Toutes les photos d'une voiture marquées principale (pour reset). */
    List<Document> findByVoiture_IdAndPrincipaleTrue(Long voitureId);
}
