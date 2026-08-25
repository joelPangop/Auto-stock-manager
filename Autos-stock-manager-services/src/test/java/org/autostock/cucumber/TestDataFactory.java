package org.autostock.cucumber;

import org.autostock.enums.Role;
import org.autostock.enums.StatutVoiture;
import org.autostock.models.*;
import org.autostock.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fabrique de données de test réutilisée par tous les fichiers de steps
 * Cucumber. Centralise la construction des entités JPA (avec leurs relations
 * obligatoires) pour que chaque fichier de steps se concentre sur les règles
 * métier plutôt que sur la plomberie de création de données.
 */
@Component
public class TestDataFactory {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MarqueRepository marqueRepository;
    @Autowired
    private ModeleRepository modeleRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private VoitureRepository voitureRepository;

    public User creerUtilisateur(String nom, Role role) {
        User u = new User();
        u.setNom(nom);
        u.setEmail(nom.toLowerCase().replace(" ", ".") + "." + UUID.randomUUID() + "@autostock.test");
        u.setMotDePasseHash("{noop}test-password");
        u.setRole(role);
        u.setAccountLocked(false);
        return userRepository.save(u);
    }

    public Marque creerMarque(String nom) {
        Marque m = new Marque();
        m.setNom(nom);
        return marqueRepository.save(m);
    }

    public Modele creerModele(Marque marque, String nom) {
        Modele m = new Modele();
        m.setMarque(marque);
        m.setNom(nom);
        return modeleRepository.save(m);
    }

    public Client creerClient(String nom) {
        Client c = new Client();
        c.setNom(nom);
        c.setEmail(nom.toLowerCase().replace(" ", ".") + "." + UUID.randomUUID() + "@client.test");
        c.setTelephone("514-000-0000");
        return clientRepository.save(c);
    }

    /** Voiture directement persistée EN_STOCK, hors service applicatif (utile pour préparer un scénario). */
    public Voiture creerVoitureEnStock(User owner, Modele modele, BigDecimal prixVente) {
        Voiture v = new Voiture();
        v.setModele(modele);
        v.setVin("VIN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        v.setAnnee(2022);
        v.setCouleur("Noir");
        v.setPrixAchat(prixVente.multiply(BigDecimal.valueOf(0.8)));
        v.setPrixVente(prixVente);
        v.setDateEntreeStock(LocalDateTime.now());
        v.setStatut(StatutVoiture.EN_STOCK);
        v.setOwner(owner);
        return voitureRepository.save(v);
    }
}
