package org.autostock.repositories;
import org.autostock.models.OptionVoiture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionVoitureRepository extends JpaRepository<OptionVoiture, Long> {

    // Toutes les options d'une voiture
//    List<OptionVoiture> findByVoiture_IdVoiture(Long voitureId);

    // Pratique si tu veux reset les options d'une voiture
//    void deleteByVoiture_IdVoiture(Long idVoiture);
}
