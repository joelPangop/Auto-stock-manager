package org.autostock.repositories;

import org.autostock.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByEmail(String email);

    Optional<Client> findByTelephone(String telephone);

    // Recherche par nom partiel
    List<Client> findByNomContainingIgnoreCase(String nom);

}
