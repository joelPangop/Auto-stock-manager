package org.autostock.services;

import org.autostock.configs.SecurityUtils;
import org.autostock.enums.StatutVoiture;
import org.autostock.enums.TypeMouvement;
import org.autostock.models.User;
import org.autostock.models.Voiture;
import org.autostock.repositories.UserRepository;
import org.autostock.repositories.VoitureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityNotFoundException;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires purs (aucun contexte Spring, aucune base) de VoitureServiceImpl.
 * Toutes les dépendances sont mockées : on vérifie ici les règles métier au
 * niveau du code, indépendamment de la persistance.
 *
 * Règles couvertes :
 *  - create() force le statut EN_STOCK, positionne owner/dateEntreeStock, trace un mouvement ENTREE.
 *  - changerStatut() n'est autorisé que pour le propriétaire ou un admin.
 *  - changerStatut() trace le mouvement correspondant au nouveau statut (sauf HORS_SERVICE).
 *  - changerStatut() sur une voiture inexistante lève EntityNotFoundException.
 */
@ExtendWith(MockitoExtension.class)
class VoitureServiceImplTest {

    @Mock
    private VoitureRepository repository;
    @Mock
    private StockMouvementService stockMouvementService;
    @Mock
    private SecurityUtils sec;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VoitureServiceImpl voitureService;

    private User owner;
    private Voiture voiture;

    @BeforeEach
    void setUp() {
        // Le champ 'repository' est declare dans AbstractBaseService avec un type
        // generique <R extends JpaRepository>. Apres effacement, @InjectMocks voit
        // plusieurs mocks assignables a JpaRepository et n'alimente pas le champ :
        // il reste null. On l'injecte donc explicitement.
        ReflectionTestUtils.setField(voitureService, "repository", repository);
        owner = new User(1L);
        voiture = new Voiture();
        voiture.setId(10L);
        voiture.setOwner(owner);
        voiture.setStatut(StatutVoiture.EN_STOCK);
    }

    @Test
    @DisplayName("create() met la voiture EN_STOCK, l'assigne au propriétaire courant et trace une ENTREE")
    void create_metLaVoitureEnStockEtTraceUneEntree() throws AccessDeniedException {
        Voiture nouvelle = new Voiture();
        when(sec.currentUserId()).thenReturn(1L);
        when(userRepository.getReferenceById(1L)).thenReturn(owner);
        when(repository.save(any(Voiture.class))).thenAnswer(inv -> inv.getArgument(0));

        Voiture resultat = voitureService.create(nouvelle);

        assertThat(resultat.getStatut()).isEqualTo(StatutVoiture.EN_STOCK);
        assertThat(resultat.getOwner()).isEqualTo(owner);
        assertThat(resultat.getDateEntreeStock()).isNotNull();
        verify(stockMouvementService).enregistrerMouvement(resultat, TypeMouvement.ENTREE, "Ajout initial au stock");
    }

    @Test
    @DisplayName("changerStatut() vers VENDUE trace un mouvement VENTE quand l'appelant est le propriétaire")
    void changerStatut_versVendue_parLeProprietaire() throws AccessDeniedException {
        when(repository.findById(10L)).thenReturn(Optional.of(voiture));
        when(sec.isAdmin()).thenReturn(false);
        when(sec.currentUserId()).thenReturn(1L);
        when(repository.save(voiture)).thenReturn(voiture);

        Voiture resultat = voitureService.changerStatut(10L, StatutVoiture.VENDUE);

        assertThat(resultat.getStatut()).isEqualTo(StatutVoiture.VENDUE);
        verify(stockMouvementService).enregistrerMouvement(voiture, TypeMouvement.VENTE, "Vente du véhicule");
    }

    @Test
    @DisplayName("changerStatut() vers HORS_SERVICE ne trace aucun mouvement (comportement actuel du switch)")
    void changerStatut_versHorsService_neTraceAucunMouvement() throws AccessDeniedException {
        when(repository.findById(10L)).thenReturn(Optional.of(voiture));
        when(sec.isAdmin()).thenReturn(false);
        when(sec.currentUserId()).thenReturn(1L);
        when(repository.save(voiture)).thenReturn(voiture);

        voitureService.changerStatut(10L, StatutVoiture.HORS_SERVICE);

        verifyNoInteractions(stockMouvementService);
    }

    @Test
    @DisplayName("changerStatut() est refusé pour un utilisateur ni propriétaire ni admin")
    void changerStatut_refusePourNonProprietaireNonAdmin() throws AccessDeniedException {
        when(repository.findById(10L)).thenReturn(Optional.of(voiture));
        when(sec.isAdmin()).thenReturn(false);
        when(sec.currentUserId()).thenReturn(2L); // != owner.id (1L)

        assertThatThrownBy(() -> voitureService.changerStatut(10L, StatutVoiture.RESERVEE))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("propriétaire");

        verify(repository, never()).save(any());
        verifyNoInteractions(stockMouvementService);
    }

    @Test
    @DisplayName("changerStatut() est autorisé pour un admin même s'il n'est pas propriétaire")
    void changerStatut_autorisePourAdminNonProprietaire() throws AccessDeniedException {
        when(repository.findById(10L)).thenReturn(Optional.of(voiture));
        when(sec.isAdmin()).thenReturn(true);
        when(repository.save(voiture)).thenReturn(voiture);

        Voiture resultat = voitureService.changerStatut(10L, StatutVoiture.RESERVEE);

        assertThat(resultat.getStatut()).isEqualTo(StatutVoiture.RESERVEE);
        verify(sec, never()).currentUserId();
    }

    @Test
    @DisplayName("changerStatut() sur une voiture inexistante lève EntityNotFoundException")
    void changerStatut_voitureInexistante() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voitureService.changerStatut(99L, StatutVoiture.RESERVEE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("introuvable");
    }
}
