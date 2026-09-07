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
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * La date de vente est saisie dans le formulaire. Elle etait ecrasee par
 * LocalDateTime.now() a chaque creation : une vente antidatee — le cas courant
 * quand la saisie est faite apres coup — etait enregistree au jour de la saisie.
 */
@ExtendWith(MockitoExtension.class)
class VenteServiceImplDateTest {

    @Mock private VenteRepository repository;
    @Mock private VoitureService voitureService;
    @Mock private ClientRepository clientRepository;
    @Mock private UserRepository userRepository;
    @Mock private StockMouvementService stockMouvementService;

    @InjectMocks
    private VenteServiceImpl venteService;

    @BeforeEach
    void setUp() {
        // Voir VoitureServiceImplTest : 'repository' vient d'AbstractBaseService
        // et son type generique efface empeche @InjectMocks de l'alimenter.
        ReflectionTestUtils.setField(venteService, "repository", repository);

        Voiture voiture = new Voiture();
        voiture.setId(10L);
        when(voitureService.findById(10L)).thenReturn(Optional.of(voiture));
        when(clientRepository.findById(2L)).thenReturn(Optional.of(new Client()));
        when(userRepository.findById(3L)).thenReturn(Optional.of(new User(3L)));
        when(repository.save(any(Vente.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Vente vendreLe(LocalDate date) throws Exception {
        return venteService.creerVente(10L, 2L, 3L, BigDecimal.valueOf(15000), "CASH", date);
    }

    @Test
    @DisplayName("la date choisie dans le formulaire est conservee")
    void dateChoisieConservee() throws Exception {
        LocalDate hier = LocalDate.now().minusDays(1);

        Vente vente = vendreLe(hier);

        assertThat(vente.getDateVente().toLocalDate()).isEqualTo(hier);
    }

    @Test
    @DisplayName("une vente antidatee n est pas ramenee au jour de la saisie")
    void venteAntidatee() throws Exception {
        LocalDate ancienne = LocalDate.of(2026, 1, 15);

        Vente vente = vendreLe(ancienne);

        assertThat(vente.getDateVente().toLocalDate())
                .as("la date de saisie ne doit pas remplacer la date de la transaction")
                .isEqualTo(ancienne)
                .isNotEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("sans date fournie, la vente est datee de maintenant")
    void dateAbsenteRetombeSurMaintenant() throws Exception {
        Vente vente = vendreLe(null);

        assertThat(vente.getDateVente().toLocalDate()).isEqualTo(LocalDate.now());
    }
}
