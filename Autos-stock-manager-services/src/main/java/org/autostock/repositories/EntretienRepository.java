package org.autostock.repositories;
import org.autostock.models.Entretien;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EntretienRepository extends JpaRepository<Entretien, Long> {

    // Historique des entretiens d'une voiture
//    List<Entretien> findByVoiture_IdVoiture(Long idVoiture);

    // Entretiens sur une période (ex: pour planning ou suivi coût)
    List<Entretien> findByDateEntretienBetween(LocalDateTime debut, LocalDateTime fin);

    List<Entretien> findByVoiture_Id(Long voitureId);

//    @Query("""
//    select e from Entretien e
//    join fetch e.voiture v
//""")
//    Page<Entretien> findAllWithVoiture(Pageable pageable);
//
//    @Query("""
//    select e from Entretien e
//    join fetch e.voiture v
//    where e.ownerId = :ownerId
//""")
//    Page<Entretien> findAllWithVoitureByOwner(@Param("ownerId") Long ownerId, Pageable pageable);

}
