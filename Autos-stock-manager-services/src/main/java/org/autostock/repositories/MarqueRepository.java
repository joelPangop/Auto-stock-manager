package org.autostock.repositories;

import org.autostock.models.Marque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarqueRepository extends JpaRepository<Marque, Long> {
    Optional<Marque> findByNomIgnoreCase(String nom);
}
