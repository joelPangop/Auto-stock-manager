package org.autostock.security;

import org.autostock.enums.Role;
import org.autostock.models.Client;
import org.autostock.models.User;
import org.autostock.repositories.ClientRepository;
import org.autostock.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Injection SQL — verification que les entrees utilisateur atteignant la base
 * sont bien traitees comme des donnees et jamais comme du code.
 *
 * <p>La protection ne vient pas d'un filtrage de caracteres mais du fait que
 * Spring Data et Hibernate produisent des requetes preparees : la valeur est
 * transmise separement de la requete. Ces tests le prouvent au lieu de le
 * supposer, et surtout ils detecteront une regression le jour ou quelqu'un
 * remplacera une requete derivee par une concatenation de chaines.
 *
 * <p>Les charges utiles couvrent les familles classiques : contournement
 * d'authentification (tautologie), destruction (DROP), extraction (UNION),
 * commentaire de fin de requete, et empilement d'instructions.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SqlInjectionTest {

    /** Charges utiles reutilisees dans plusieurs tests. */
    static final String[] CHARGES = {
            "' OR '1'='1",
            "' OR 1=1 --",
            "admin'--",
            "'; DROP TABLE utilisateur; --",
            "' UNION SELECT null, null, null, null, null --",
            "1; DELETE FROM utilisateur WHERE '1'='1",
            "\" OR \"\"=\"",
            "') OR ('1'='1"
    };

    @Autowired private UserRepository userRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private EntityManager em;

    @BeforeEach
    void seed() {
        userRepository.save(User.builder()
                .nom("Legitime")
                .email("legitime@test.fr")
                .motDePasseHash("$2a$10$hashfactice")
                .role(Role.USER)
                .accountLocked(false)
                .build());

        var client = new Client();
        client.setNom("Dupont");
        client.setEmail("dupont@test.fr");
        clientRepository.save(client);

        em.flush();
    }

    // =====================================================================
    // Recherche par email — surface d'authentification
    // =====================================================================

    @ParameterizedTest(name = "findByEmail refuse la charge : {0}")
    @ValueSource(strings = {
            "' OR '1'='1",
            "' OR 1=1 --",
            "admin'--",
            "' UNION SELECT null --",
            "') OR ('1'='1"
    })
    @DisplayName("une tautologie dans l'email ne fait remonter aucun compte")
    void findByEmail_neRetournePasDeCompteSurTautologie(String charge) {
        var trouve = userRepository.findByEmail(charge);

        assertThat(trouve)
                .as("la charge est traitee comme une valeur litterale, pas comme du SQL")
                .isEmpty();
    }

    @Test
    @DisplayName("findByEmailIgnoreCase resiste aux memes charges")
    void findByEmailIgnoreCase_resiste() {
        for (String charge : CHARGES) {
            assertThat(userRepository.findByEmailIgnoreCase(charge)).isEmpty();
        }
    }

    // =====================================================================
    // Recherche texte libre — surface la plus exposee cote metier
    // =====================================================================

    @ParameterizedTest(name = "recherche client, charge : {0}")
    @ValueSource(strings = {
            "' OR '1'='1",
            "%' OR '1'='1",
            "'; DROP TABLE client; --",
            "' UNION SELECT null, null, null, null --"
    })
    @DisplayName("une charge dans la recherche par nom ne retourne pas toute la table")
    void rechercheClient_neFuitPasLaTable(String charge) {
        var resultats = clientRepository.findByNomContainingIgnoreCase(charge);

        assertThat(resultats)
                .as("une injection reussie ferait remonter Dupont, qui ne correspond pourtant pas")
                .isEmpty();
    }

    @Test
    @DisplayName("le caractere joker % est traite litteralement et ne fait pas tout remonter")
    void rechercheClient_leJokerNestPasInterprete() {
        // Cas subtil : LIKE est parametre, mais un % passe en valeur reste un
        // joker cote SQL. Ce test documente le comportement reel plutot que
        // de supposer qu'il est neutre.
        var resultats = clientRepository.findByNomContainingIgnoreCase("%");

        assertThat(resultats)
                .as("si ce test casse, c'est que le % passe desormais pour un joker : "
                        + "il faudra l'echapper avant de construire le LIKE")
                .isEmpty();
    }

    // =====================================================================
    // Integrite : la base survit aux charges destructrices
    // =====================================================================

    @Test
    @DisplayName("apres toutes les charges, les tables et les donnees sont intactes")
    void laBaseResteIntacte() {
        for (String charge : CHARGES) {
            userRepository.findByEmail(charge);
            userRepository.findByEmailIgnoreCase(charge);
            clientRepository.findByNomContainingIgnoreCase(charge);
        }

        assertThat(userRepository.count())
                .as("aucune ligne supprimee par les charges DELETE/DROP")
                .isEqualTo(1);
        assertThat(clientRepository.count()).isEqualTo(1);
        assertThat(userRepository.findByEmail("legitime@test.fr"))
                .as("le compte legitime est toujours lisible")
                .isPresent();
    }

    // =====================================================================
    // Tri paginé — la seule entree utilisateur qui n'est pas une valeur
    // =====================================================================

    @Test
    @DisplayName("un nom de propriete de tri arbitraire est rejete, pas execute")
    void tri_uneProprieteInconnueEstRejetee() {
        // parseSort() des services de paiement et d'entretien passe la chaine
        // recue du client directement a Sort.by(). Contrairement aux valeurs,
        // un nom de propriete n'est PAS parametrable : la protection repose
        // entierement sur le fait que Spring Data le resout contre le modele
        // et refuse ce qu'il ne connait pas.
        assertThatThrownBy(() -> userRepository.findAll(
                PageRequest.of(0, 10, Sort.by("nom; DROP TABLE utilisateur --"))))
                .isInstanceOf(PropertyReferenceException.class);

        assertThat(userRepository.count())
                .as("la table existe toujours")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("un tri sur une propriete legitime fonctionne toujours")
    void tri_proprieteLegitime() {
        var page = userRepository.findAll(PageRequest.of(0, 10, Sort.by("email")));

        assertThat(page.getContent()).hasSize(1);
    }
}
