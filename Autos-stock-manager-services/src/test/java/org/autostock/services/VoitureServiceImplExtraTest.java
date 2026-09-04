package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.configs.SecurityUtils;
import org.autostock.enums.StatutVoiture;
import org.autostock.enums.TypeMouvement;
import org.autostock.models.User;
import org.autostock.models.Voiture;
import org.autostock.repositories.UserRepository;
import org.autostock.repositories.VoitureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Complement a VoitureServiceImplTest : consultation, mise a jour et
 * traçabilite des changements de statut.
 *
 * <p>La regle transverse est le cloisonnement par proprietaire. Un vendeur ne
 * doit agir que sur ses propres vehicules ; seul un admin passe outre. Le test
 * de update() ci-dessous documente au passage un comportement du code actuel
 * qui merite discussion — voir son commentaire.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VoitureServiceImplExtraTest {

    @Mock private VoitureRepository repository;
    @Mock private StockMouvementService stockMouvementService;
    @Mock private SecurityUtils sec;
    @Mock private UserRepository userRepository;

    private VoitureServiceImpl service;

    private User proprietaire;
    private Voiture voiture;

    @BeforeEach
    void setUp() {
        service = new VoitureServiceImpl();
        ReflectionTestUtils.setField(service, "repository", repository);
        ReflectionTestUtils.setField(service, "stockMouvementService", stockMouvementService);
        ReflectionTestUtils.setField(service, "sec", sec);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);

        proprietaire = new User(1L);

        voiture = new Voiture();
        voiture.setId(10L);
        voiture.setOwner(proprietaire);
        voiture.setStatut(StatutVoiture.EN_STOCK);

        when(repository.save(any(Voiture.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.findById(10L)).thenReturn(Optional.of(voiture));
    }

    // =====================================================================
    @Nested
    @DisplayName("Consultation")
    class Consultation {

        @Test
        @DisplayName("le stock ne remonte que les vehicules EN_STOCK")
        void listerEnStock() {
            when(repository.findByStatut(StatutVoiture.EN_STOCK)).thenReturn(List.of(voiture));

            assertThat(service.listerVoituresEnStock()).containsExactly(voiture);
            verify(repository).findByStatut(StatutVoiture.EN_STOCK);
        }

        @Test
        @DisplayName("listMine est filtre sur l utilisateur connecte, pas sur un identifiant recu")
        void listMine_filtreSurLUtilisateurConnecte() throws Exception {
            when(sec.currentUserId()).thenReturn(1L);
            when(repository.findByOwner_Id(1L)).thenReturn(List.of(voiture));

            assertThat(service.listMine()).containsExactly(voiture);
            verify(repository).findByOwner_Id(1L);
        }

        @Test
        @DisplayName("la recherche par VIN remonte le vehicule correspondant")
        void trouverParVin() {
            when(repository.findByVin("VF1ABCDEF12345678")).thenReturn(Optional.of(voiture));

            assertThat(service.trouverParVin("VF1ABCDEF12345678")).contains(voiture);
        }

        @Test
        @DisplayName("un VIN inconnu renvoie un Optional vide, pas une exception")
        void trouverParVin_inconnu() {
            when(repository.findByVin("INCONNU")).thenReturn(Optional.empty());

            assertThat(service.trouverParVin("INCONNU")).isEmpty();
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Tracabilite des changements de statut")
    class Tracabilite {

        @Test
        @DisplayName("passer en VENDUE trace un mouvement de vente")
        void versVendue() throws Exception {
            when(sec.isAdmin()).thenReturn(true);

            service.changerStatut(10L, StatutVoiture.VENDUE);

            verify(stockMouvementService).enregistrerMouvement(
                    eq(voiture), eq(TypeMouvement.VENTE), any());
        }

        @Test
        @DisplayName("passer en RESERVEE trace un mouvement de reservation")
        void versReservee() throws Exception {
            when(sec.isAdmin()).thenReturn(true);

            service.changerStatut(10L, StatutVoiture.RESERVEE);

            verify(stockMouvementService).enregistrerMouvement(
                    eq(voiture), eq(TypeMouvement.RESERVATION), any());
        }

        @Test
        @DisplayName("un retour EN_STOCK trace un mouvement de retour")
        void versEnStock() throws Exception {
            when(sec.isAdmin()).thenReturn(true);
            voiture.setStatut(StatutVoiture.RESERVEE);

            service.changerStatut(10L, StatutVoiture.EN_STOCK);

            verify(stockMouvementService).enregistrerMouvement(
                    eq(voiture), eq(TypeMouvement.RETOUR), any());
        }

        @Test
        @DisplayName("le statut est reellement applique au vehicule")
        void statutApplique() throws Exception {
            when(sec.isAdmin()).thenReturn(true);

            var maj = service.changerStatut(10L, StatutVoiture.VENDUE);

            assertThat(maj.getStatut()).isEqualTo(StatutVoiture.VENDUE);
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Mise a jour")
    class MiseAJour {

        private Voiture patch() {
            var v = new Voiture();
            v.setOwner(proprietaire);
            v.setCouleur("Rouge");
            return v;
        }

        @Test
        @DisplayName("le proprietaire peut modifier son vehicule")
        void parLeProprietaire() throws Exception {
            when(sec.isAdmin()).thenReturn(false);
            when(sec.currentUserId()).thenReturn(1L);

            var maj = service.update(10L, patch());

            assertThat(maj.getCouleur()).isEqualTo("Rouge");
        }

        @Test
        @DisplayName("un admin peut modifier le vehicule d autrui")
        void parUnAdmin() throws Exception {
            when(sec.isAdmin()).thenReturn(true);

            assertThat(service.update(10L, patch())).isNotNull();
        }

        @Test
        @DisplayName("un tiers non admin est refuse et rien n est enregistre")
        void parUnTiersRefusee() throws Exception {
            when(sec.isAdmin()).thenReturn(false);
            when(sec.currentUserId()).thenReturn(2L);

            assertThatThrownBy(() -> service.update(10L, patch()))
                    .isInstanceOf(AccessDeniedException.class);

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("la mise a jour ne peut pas changer le proprietaire ni la date d entree")
        void champsProtegesConserves() throws Exception {
            when(sec.isAdmin()).thenReturn(true);
            var dateOrigine = java.time.LocalDateTime.of(2026, 1, 1, 8, 0);
            voiture.setDateEntreeStock(dateOrigine);

            var intrus = new User(99L);
            var patch = new Voiture();
            patch.setOwner(intrus);
            patch.setDateEntreeStock(java.time.LocalDateTime.now());

            var maj = service.update(10L, patch);

            assertThat(maj.getOwner())
                    .as("le proprietaire vient de la base, pas du corps de la requete")
                    .isSameAs(proprietaire);
            assertThat(maj.getDateEntreeStock())
                    .as("l entree en stock est un fait historique, pas un champ editable")
                    .isEqualTo(dateOrigine);
        }

        @Test
        @DisplayName("la mise a jour conserve l identifiant cible")
        void identifiantConserve() throws Exception {
            when(sec.isAdmin()).thenReturn(true);

            assertThat(service.update(10L, patch()).getId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("modifier un vehicule inexistant echoue")
        void vehiculeInexistant() {
            when(repository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(404L, patch()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("un tiers qui se declare proprietaire dans le patch reste refuse")
        void proprietaireDuPatchIgnore() throws Exception {
            // Le controle porte sur le proprietaire en base, pas sur celui du
            // corps de la requete : se declarer owner ne donne aucun droit.
            when(sec.isAdmin()).thenReturn(false);
            when(sec.currentUserId()).thenReturn(2L);

            var patchDUnTiers = new Voiture();
            patchDUnTiers.setOwner(new User(2L)); // le tiers se declare proprietaire

            assertThatThrownBy(() -> service.update(10L, patchDUnTiers))
                    .isInstanceOf(AccessDeniedException.class);

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("un patch sans proprietaire ne fait pas echouer le controle")
        void patchSansProprietaire() throws Exception {
            // Le mapper ne renseigne jamais owner : le controle doit rester
            // fonctionnel sans NullPointerException.
            when(sec.isAdmin()).thenReturn(false);
            when(sec.currentUserId()).thenReturn(1L);

            var patchSansOwner = new Voiture();
            patchSansOwner.setCouleur("Bleu");

            assertThat(service.update(10L, patchSansOwner).getCouleur()).isEqualTo("Bleu");
        }
    }
}
