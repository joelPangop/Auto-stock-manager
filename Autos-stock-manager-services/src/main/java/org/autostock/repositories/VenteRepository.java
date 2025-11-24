package org.autostock.repositories;

import org.autostock.models.Vente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VenteRepository extends JpaRepository<Vente, Long> {

    List<Vente> findByClient_Id(Long clientId);

    List<Vente> findByVendeur_Id(Long vendeurId);

    List<Vente> findByDateVenteBetween(LocalDateTime debut, LocalDateTime fin);
}
