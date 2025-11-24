package org.autostock.repositories;
import org.autostock.models.Entretien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EntretienRepository extends JpaRepository<Entretien, Long> {

    // Historique des entretiens d'une voiture
//    List<Entretien> findByVoiture_IdVoiture(Long idVoiture);

    // Entretiens sur une période (ex: pour planning ou suivi coût)
    List<Entretien> findByDateEntretienBetween(LocalDateTime debut, LocalDateTime fin);

    List<Entretien> findByVoiture_Id(Long voitureId);
}
