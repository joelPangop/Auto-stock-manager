package org.autostock.services;

import org.autostock.enums.TypeMouvement;
import org.autostock.models.StockMouvement;
import org.autostock.models.Voiture;
import org.autostock.repositories.StockMouvementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Journal des mouvements de stock.
 *
 * <p>C'est la piste d'audit du parc : chaque entree, vente, reservation ou
 * retour doit y laisser une trace horodatee. Un mouvement non enregistre est
 * invisible et irrattrapable, d'ou l'insistance sur l'horodatage automatique.
 */
@ExtendWith(MockitoExtension.class)
class StockMouvementServiceImplTest {

    @Mock private StockMouvementRepository repository;

    private StockMouvementServiceImpl service;

    private Voiture voiture;

    @BeforeEach
    void setUp() {
        service = new StockMouvementServiceImpl(repository);
        voiture = new Voiture();
        voiture.setId(1L);

        // lenient : les tests de lecture (historiqueVoiture) n'ecrivent rien et
        // les stubs stricts signaleraient ce stub comme inutile chez eux.
        lenient().when(repository.save(any(StockMouvement.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("un mouvement porte la voiture, le type et le commentaire fournis")
    void enregistrerMouvement_renseigneLesChamps() {
        var m = service.enregistrerMouvement(voiture, TypeMouvement.ENTREE, "Ajout initial au stock");

        assertThat(m.getVoiture()).isSameAs(voiture);
        assertThat(m.getType()).isEqualTo(TypeMouvement.ENTREE);
        assertThat(m.getCommentaire()).isEqualTo("Ajout initial au stock");
    }

    @Test
    @DisplayName("la date du mouvement est posee par le service, jamais laissee a l appelant")
    void enregistrerMouvement_horodateAutomatiquement() {
        var avant = LocalDateTime.now().minusSeconds(1);

        var m = service.enregistrerMouvement(voiture, TypeMouvement.VENTE, "Vente");

        assertThat(m.getDateMouvement())
                .as("une trace d audit sans horodatage ne sert a rien")
                .isNotNull()
                .isAfter(avant);
    }

    @ParameterizedTest
    @EnumSource(TypeMouvement.class)
    @DisplayName("tous les types de mouvement sont acceptes tels quels")
    void enregistrerMouvement_accepteTousLesTypes(TypeMouvement type) {
        var m = service.enregistrerMouvement(voiture, type, "commentaire");

        assertThat(m.getType()).isEqualTo(type);
    }

    @Test
    @DisplayName("le mouvement est bien persiste")
    void enregistrerMouvement_persiste() {
        service.enregistrerMouvement(voiture, TypeMouvement.RETOUR, "Annulation");

        var capture = ArgumentCaptor.forClass(StockMouvement.class);
        org.mockito.Mockito.verify(repository).save(capture.capture());
        assertThat(capture.getValue().getType()).isEqualTo(TypeMouvement.RETOUR);
    }

    @Test
    @DisplayName("un commentaire nul ne bloque pas l enregistrement")
    void enregistrerMouvement_toleresUnCommentaireNul() {
        var m = service.enregistrerMouvement(voiture, TypeMouvement.ENTREE, null);

        assertThat(m.getCommentaire()).isNull();
        assertThat(m.getType()).isEqualTo(TypeMouvement.ENTREE);
    }

    @Test
    @DisplayName("l historique est lu pour la voiture demandee")
    void historiqueVoiture_delegueAuRepository() {
        var m1 = new StockMouvement();
        var m2 = new StockMouvement();
        when(repository.findByVoiture_Id(1L)).thenReturn(List.of(m1, m2));

        assertThat(service.historiqueVoiture(1L)).containsExactly(m1, m2);
    }

    @Test
    @DisplayName("une voiture sans mouvement renvoie une liste vide, pas null")
    void historiqueVoiture_listeVide() {
        when(repository.findByVoiture_Id(99L)).thenReturn(List.of());

        assertThat(service.historiqueVoiture(99L)).isNotNull().isEmpty();
    }
}
