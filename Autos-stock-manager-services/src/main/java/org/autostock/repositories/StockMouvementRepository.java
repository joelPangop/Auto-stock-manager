package org.autostock.repositories;

import org.autostock.enums.TypeMouvement;
import org.autostock.models.StockMouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMouvementRepository extends JpaRepository<StockMouvement, Long> {

    // Historique complet d'une voiture, du plus récent au plus ancien
//    List<StockMouvement> findByVoiture_IdVoitureOrderByDateMouvementDesc(Long voitureId);

    // Tous les mouvements d'un certain type (ENTREE, VENTE, etc.)
    List<StockMouvement> findByType(TypeMouvement type);
    List<StockMouvement> findByVoiture_Id(Long voitureId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT m FROM StockMouvement m " +
        "LEFT JOIN FETCH m.voiture v " +
        "LEFT JOIN FETCH v.modele mo " +
        "LEFT JOIN FETCH mo.marque " +
        "ORDER BY m.dateMouvement DESC"
    )
    List<StockMouvement> findAllWithVoiture();
}
