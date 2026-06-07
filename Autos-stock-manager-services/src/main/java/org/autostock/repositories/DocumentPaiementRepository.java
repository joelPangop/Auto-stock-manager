package org.autostock.repositories;

import org.autostock.models.DocumentPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentPaiementRepository extends JpaRepository<DocumentPaiement, Long> {
}
