package org.autostock.services;

import org.autostock.models.Client;
import org.autostock.models.User;
import org.autostock.models.Vente;
import org.autostock.models.Voiture;
import org.autostock.repositories.ClientRepository;
import org.autostock.repositories.UserRepository;
import org.autostock.repositories.VenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Le prix de vente d'une voiture n'est plus saisissable sur sa fiche : il
 * decoule de la vente. C'est donc la vente qui doit le renseigner, sans quoi
 * le montant resterait vide et le benefice incalculable.
 */
@ExtendWith(MockitoExtension.class)
class VenteServiceImplPrixVenteTest {

    @Mock private VenteRepository repository;
    @Mock private VoitureService voitureService;
    @Mock private ClientRepository clientRepository;
    @Mock private UserRepository userRepository;
    @Mock private StockMouvementService stockMouvementService;

    @InjectMocks private VenteServiceImpl venteService;

    private Voiture voiture;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(venteService, "repository", repository);
        voiture = new Voiture();
        voiture.setId(10L);
    }

    private void contexteDeVente() {
        when(voitureService.findById(10L)).thenReturn(Optional.of(voiture));
        when(clientRepository.findById(2L)).thenReturn(Optional.of(new Client()));
        when(userRepository.findById(3L)).thenReturn(Optional.of(new User(3L)));
        when(repository.save(any(Vente.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("creer une vente renseigne le prix de vente de la voiture")
    void creationRenseigneLePrix() throws Exception {
        contexteDeVente();

        venteService.creerVente(10L, 2L, 3L, new BigDecimal("18000"), "CASH", null);

        assertThat(voiture.getPrixVente()).isEqualByComparingTo("18000");
    }

    @Test
    @DisplayName("corriger le prix d une vente met la voiture a jour")
    void modificationSuitLaVente() {
        Vente vente = new Vente();
        vente.setId(4L);
        vente.setVoiture(voiture);
        voiture.setPrixVente(new BigDecimal("18000"));
        when(repository.findById(4L)).thenReturn(Optional.of(vente));
        when(repository.save(any(Vente.class))).thenAnswer(i -> i.getArgument(0));

        venteService.modifierVente(4L, null, null, null, new BigDecimal("17000"), null);

        assertThat(voiture.getPrixVente()).isEqualByComparingTo("17000");
    }

    @Test
    @DisplayName("modifier une vente sans toucher au prix laisse la voiture inchangee")
    void prixInchangeSiAbsent() {
        Vente vente = new Vente();
        vente.setId(4L);
        vente.setVoiture(voiture);
        voiture.setPrixVente(new BigDecimal("18000"));
        when(repository.findById(4L)).thenReturn(Optional.of(vente));
        when(repository.save(any(Vente.class))).thenAnswer(i -> i.getArgument(0));

        venteService.modifierVente(4L, null, null, null, null, "CARD");

        assertThat(voiture.getPrixVente()).isEqualByComparingTo("18000");
    }
}
