package org.autostock.repositories;

import org.autostock.enums.Role;
import org.autostock.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Vente liée à une voiture donnée (1 voiture = 1 vente max)
//    Optional<Vente> findByVoiture_IdVoiture(Long voitureId);
//
//    // Toutes les ventes d'un client
//    List<Vente> findByClient_IdClient(Long clientId);
//
//    // Toutes les ventes faites par un vendeur
//    List<Vente> findByVendeur_IdUser(Long vendeurId);

    // Ventes sur une période (ex: pour un rapport journalier/mensuel)
//    List<Vente> findByDateVenteBetween(LocalDateTime debut, LocalDateTime fin);

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);
}
