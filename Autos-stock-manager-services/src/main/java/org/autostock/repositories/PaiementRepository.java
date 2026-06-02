package org.autostock.repositories;
import org.autostock.enums.MethodePaiement;
import org.autostock.models.Paiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    // Tous les paiements d'une vente
//    List<Paiement> findByVente_IdVente(Long idVente);

    // Paiements sur une période (ex: pour la trésorerie)
//    List<Paiement> findByDatePaiementBetween(LocalDateTime debut, LocalDateTime fin);

    // Tous les paiements d'un certain type
    List<Paiement> findByMethode(MethodePaiement methode);

    List<Paiement> findByVente_Id(Long venteId);

    // --- LISTE PAGINÉE (TOUS) ---
    @EntityGraph(attributePaths = {
            "vente",
            "vente.voiture"
    })
    Page<Paiement> findAll(Pageable pageable);

    // --- LISTE PAGINÉE (onlyMine) ---
    @EntityGraph(attributePaths = {
            "vente",
            "vente.voiture"
    })
    @Query("""
        select p from Paiement p
        join p.vente v
        where v.voiture.owner.id = :userId
    """)
    Page<Paiement> findAllByUser(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // --- GET BY ID ---
    @EntityGraph(attributePaths = {
            "vente",
            "vente.voiture"
    })
    Optional<Paiement> findById(Long id);

    @Query("""
    select sum(p.montant) from Paiement p
    where p.vente.id = :venteId
""")
    BigDecimal sumMontantByVenteId(Long venteId);

    @Modifying
    @Query("""
    update Vente v set v.prixFinal = :total
    where v.id = :venteId
""")
    void updateTotalPaye(Long venteId, BigDecimal total);


}
