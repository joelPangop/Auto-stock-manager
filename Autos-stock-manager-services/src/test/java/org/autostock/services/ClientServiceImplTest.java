package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.models.Client;
import org.autostock.models.Vente;
import org.autostock.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * La modification d'un client ne doit reprendre que les champs du formulaire.
 * Recopier l'entite recue effacerait l'historique des ventes et la version
 * optimiste — l'erreur exacte qui rendait la mise a jour d'une voiture
 * dangereuse avant correction.
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock private ClientRepository repository;

    @InjectMocks private ClientServiceImpl clientService;

    private Client existant;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(clientService, "repository", repository);
        existant = new Client();
        existant.setId(5L);
        existant.setNom("Ancien Nom");
        existant.setEmail("ancien@test.fr");
        existant.setVentes(List.of(new Vente()));
    }

    private Client patch(String nom, String email) {
        Client c = new Client();
        c.setNom(nom);
        c.setEmail(email);
        return c;
    }

    @Test
    @DisplayName("les champs du formulaire sont repris")
    void champsRepris() {
        when(repository.findById(5L)).thenReturn(Optional.of(existant));
        when(repository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        Client maj = clientService.modifier(5L, patch("Nouveau Nom", "nouveau@test.fr"));

        assertThat(maj.getNom()).isEqualTo("Nouveau Nom");
        assertThat(maj.getEmail()).isEqualTo("nouveau@test.fr");
    }

    @Test
    @DisplayName("l identifiant et l historique des ventes sont preserves")
    void champsProtegesConserves() {
        when(repository.findById(5L)).thenReturn(Optional.of(existant));
        when(repository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        Client maj = clientService.modifier(5L, patch("Nouveau Nom", "nouveau@test.fr"));

        assertThat(maj.getId()).isEqualTo(5L);
        assertThat(maj.getVentes())
                .as("les ventes ne figurent pas dans le formulaire et ne doivent pas disparaitre")
                .hasSize(1);
    }

    @Test
    @DisplayName("modifier un client inexistant echoue explicitement")
    void clientInexistant() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.modifier(404L, patch("X", "x@test.fr")))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
