package org.autostock.repositories;

import org.autostock.models.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    // Recherche par type (Concessionnaire, Particulier, etc.)
    List<Fournisseur> findByTypeIgnoreCase(String type);

    // Recherche par nom partiel
    List<Fournisseur> findByNomContainingIgnoreCase(String nom);
}
