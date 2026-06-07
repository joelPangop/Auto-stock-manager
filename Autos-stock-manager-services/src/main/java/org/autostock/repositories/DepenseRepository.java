package org.autostock.repositories;

import org.autostock.enums.CategorieDepense;
import org.autostock.models.Depense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface DepenseRepository extends JpaRepository<Depense, Long> {

    /* =========================
       Requêtes de base
       ========================= */

    List<Depense> findAllByVoiture_Id(Long voitureId);

    List<Depense> findAllByVoiture_IdOrderByDateDepenseDesc(Long voitureId);

    List<Depense> findAllByVoiture_IdAndCategorie(
            Long voitureId,
            CategorieDepense categorie
    );

    List<Depense> findAllByFournisseur_Id(Long fournisseurId);

    List<Depense> findAllByDocument_Id(Long documentId);

    List<Depense> findAllByEntretien_Id(Long entretienId);

    /* =========================
       Totaux / statistiques
       ========================= */

    @Query("""
           SELECT COALESCE(SUM(d.montant), 0)
           FROM Depense d
           WHERE d.voiture.id = :voitureId
           """)
    BigDecimal totalByVoiture(@Param("voitureId") Long voitureId);

    @Query("""
           SELECT COALESCE(SUM(d.montant), 0)
           FROM Depense d
           WHERE d.voiture.id = :voitureId
             AND d.categorie = :categorie
           """)
    BigDecimal totalByVoitureAndCategorie(
            @Param("voitureId") Long voitureId,
            @Param("categorie") CategorieDepense categorie
    );

    @Query("""
           SELECT COALESCE(SUM(d.montant), 0)
           FROM Depense d
           WHERE d.voiture.id = :voitureId
             AND d.dateDepense BETWEEN :start AND :end
           """)
    BigDecimal totalByVoitureAndPeriod(
            @Param("voitureId") Long voitureId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /* =========================
       Filtres avancés (optionnels)
       ========================= */

    List<Depense> findAllByVoiture_IdAndDateDepenseBetween(
            Long voitureId,
            LocalDateTime start,
            LocalDateTime end
    );

    // ✅ Pagination (par voiture)
    Page<Depense> findByVoiture_Id(Long voitureId, Pageable pageable);
    Depense findByVoiture_IdAndId(Long voitureId, Long id);
    void deleteByVoiture_IdAndId(Long voitureId, Long id);
    void deleteByEntretienId(Long entretienId);

    // ✅ Pagination + filtres optionnels
    Page<Depense> findByVoiture_IdAndCategorie(Long voitureId, CategorieDepense categorie, Pageable pageable);

    Page<Depense> findByVoiture_IdAndDateDepenseBetween(Long voitureId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // ✅ Non justifiées (document IS NULL AND entretien IS NULL)
    Page<Depense> findByVoiture_IdAndDocumentIsNullAndEntretienIsNull(Long voitureId, Pageable pageable);

    // ✅ Graph mensuel (MySQL): YEAR/MONTH
    @Query("""
           SELECT YEAR(d.dateDepense) as year,
                  MONTH(d.dateDepense) as month,
                  COALESCE(SUM(d.montant),0) as total
           FROM Depense d
           WHERE d.voiture.id = :voitureId
             AND d.dateDepense BETWEEN :start AND :end
           GROUP BY YEAR(d.dateDepense), MONTH(d.dateDepense)
           ORDER BY YEAR(d.dateDepense), MONTH(d.dateDepense)
           """)
    List<MensuelTotalProjection> monthlyTotals(@Param("voitureId") Long voitureId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    // ✅ Graph mensuel par catégorie
    @Query("""
           SELECT YEAR(d.dateDepense) as year,
                  MONTH(d.dateDepense) as month,
                  d.categorie as categorie,
                  COALESCE(SUM(d.montant),0) as total
           FROM Depense d
           WHERE d.voiture.id = :voitureId
             AND d.dateDepense BETWEEN :start AND :end
           GROUP BY YEAR(d.dateDepense), MONTH(d.dateDepense), d.categorie
           ORDER BY YEAR(d.dateDepense), MONTH(d.dateDepense), d.categorie
           """)
    List<MensuelCategorieTotalProjection> monthlyTotalsByCategorie(@Param("voitureId") Long voitureId,
                                                                   @Param("start") LocalDateTime start,
                                                                   @Param("end") LocalDateTime end);
}
