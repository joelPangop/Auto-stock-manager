package org.autostock.repositories;

import org.autostock.models.Modele;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModeleRepository extends JpaRepository<Modele, Long> {
    List<Modele> findByMarque_Id(Long idMarque);
    // Tous les modèles d'une marque (par id marque)
//    List<Modele> findByMarque_IdMarque(Long marqueId);

    // Tous les modèles d'une marque (par nom de marque)
//    List<Modele> findByMarque_NomIgnoreCase(String nomMarque);
}
