package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.mappers.EntretienMapper;
import org.autostock.models.Entretien;
import org.autostock.models.Voiture;
import org.autostock.repositories.EntretienRepository;
import org.autostock.repositories.VoitureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Entretiens rattaches a un vehicule.
 *
 * <p>Le tri paginé est teste explicitement : getPage transmet a Sort.by() une
 * chaine venue du client. C'est la seule entree utilisateur du module qui ne
 * soit pas une valeur liee, donc la seule dont le comportement merite d'etre
 * fige (voir aussi SqlInjectionTest).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EntretienServiceImplTest {

    @Mock private EntretienRepository repository;
    @Mock private VoitureRepository voitureRepository;
    @Mock private EntretienMapper mapper;

    private EntretienServiceImpl service;

    private Voiture voiture;

    @BeforeEach
    void setUp() {
        service = new EntretienServiceImpl();
        ReflectionTestUtils.setField(service, "repository", repository);
        ReflectionTestUtils.setField(service, "voitureRepository", voitureRepository);
        ReflectionTestUtils.setField(service, "mapper", mapper);

        voiture = new Voiture();
        voiture.setId(1L);

        when(repository.save(any(Entretien.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // =====================================================================

    @Test
    @DisplayName("un entretien est rattache a la voiture ciblee")
    void ajouter_rattacheALaVoiture() {
        when(voitureRepository.findById(1L)).thenReturn(Optional.of(voiture));

        var e = new Entretien();
        e.setDateEntretien(LocalDateTime.now());

        var saved = service.ajouterEntretien(1L, e);

        assertThat(saved.getVoiture()).isSameAs(voiture);
    }

    @Test
    @DisplayName("sans date fournie, la date du jour est appliquee")
    void ajouter_dateParDefaut() {
        when(voitureRepository.findById(1L)).thenReturn(Optional.of(voiture));
        var avant = LocalDateTime.now().minusSeconds(1);

        var saved = service.ajouterEntretien(1L, new Entretien());

        assertThat(saved.getDateEntretien()).isNotNull().isAfter(avant);
    }

    @Test
    @DisplayName("une date fournie n est pas ecrasee")
    void ajouter_conserveLaDateFournie() {
        when(voitureRepository.findById(1L)).thenReturn(Optional.of(voiture));
        var date = LocalDateTime.of(2026, 1, 15, 10, 0);

        var e = new Entretien();
        e.setDateEntretien(date);

        assertThat(service.ajouterEntretien(1L, e).getDateEntretien()).isEqualTo(date);
    }

    @Test
    @DisplayName("un entretien sur une voiture inexistante est refuse et rien n est enregistre")
    void ajouter_voitureInexistante() {
        when(voitureRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ajouterEntretien(404L, new Entretien()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Voiture introuvable");

        verify(repository, never()).save(any());
    }

    // =====================================================================

    @Test
    @DisplayName("la modification remplace les champs metier de l entretien existant")
    void modifier_remplaceLesChamps() {
        var existant = new Entretien();
        existant.setId(5L);
        existant.setGarage("Ancien garage");
        existant.setCout(new java.math.BigDecimal("100.00"));
        when(repository.findById(5L)).thenReturn(Optional.of(existant));

        var patch = new Entretien();
        patch.setGarage("Nouveau garage");
        patch.setCout(new java.math.BigDecimal("250.00"));
        patch.setDescription("Revision complete");
        patch.setDateEntretien(LocalDateTime.of(2026, 3, 1, 9, 0));

        var maj = service.modifierEntretien(5L, patch);

        assertThat(maj.getGarage()).isEqualTo("Nouveau garage");
        assertThat(maj.getCout()).isEqualByComparingTo(new java.math.BigDecimal("250.00"));
        assertThat(maj.getDescription()).isEqualTo("Revision complete");
    }

    @Test
    @DisplayName("la modification conserve l identifiant d origine")
    void modifier_conserveLIdentifiant() {
        var existant = new Entretien();
        existant.setId(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(existant));

        var maj = service.modifierEntretien(5L, new Entretien());

        assertThat(maj.getId())
                .as("modifier ne doit jamais creer une seconde ligne")
                .isEqualTo(5L);
    }

    @Test
    @DisplayName("une voiture absente du patch ne detache pas l entretien")
    void modifier_neDetachePasLaVoiture() {
        var existant = new Entretien();
        existant.setId(5L);
        existant.setVoiture(voiture);
        when(repository.findById(5L)).thenReturn(Optional.of(existant));

        var maj = service.modifierEntretien(5L, new Entretien());

        assertThat(maj.getVoiture())
                .as("un patch partiel ne doit pas orpheliner l entretien")
                .isSameAs(voiture);
    }

    @Test
    @DisplayName("modifier un entretien inexistant echoue")
    void modifier_entretienInexistant() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.modifierEntretien(404L, new Entretien()))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    // =====================================================================

    @Test
    @DisplayName("les entretiens sont lus pour la voiture demandee")
    void entretiensVoiture() {
        var e = new Entretien();
        when(repository.findByVoiture_Id(1L)).thenReturn(List.of(e));

        assertThat(service.entretiensVoiture(1L)).containsExactly(e);
    }

    // =====================================================================
    // Tri paginé
    // =====================================================================

    private Page<Entretien> pageVide() {
        return new PageImpl<>(List.of());
    }

    @Test
    @DisplayName("sans parametre de tri, le tri par defaut est la date decroissante")
    void page_triParDefaut() {
        when(repository.findAll(any(Pageable.class))).thenReturn(pageVide());

        service.getPage(0, 10, null, false);

        var capture = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(capture.capture());

        var ordre = capture.getValue().getSort().getOrderFor("dateEntretien");
        assertThat(ordre).isNotNull();
        assertThat(ordre.isDescending()).isTrue();
    }

    @Test
    @DisplayName("le sens ascendant demande par le client est respecte")
    void page_triAscendant() {
        when(repository.findAll(any(Pageable.class))).thenReturn(pageVide());

        service.getPage(0, 10, "cout,asc", false);

        var capture = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(capture.capture());

        var ordre = capture.getValue().getSort().getOrderFor("cout");
        assertThat(ordre).isNotNull();
        assertThat(ordre.isAscending()).isTrue();
    }

    @Test
    @DisplayName("un sens inconnu retombe sur decroissant plutot que d echouer")
    void page_sensInconnu() {
        when(repository.findAll(any(Pageable.class))).thenReturn(pageVide());

        service.getPage(0, 10, "cout,nimportequoi", false);

        var capture = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(capture.capture());

        assertThat(capture.getValue().getSort().getOrderFor("cout").isDescending()).isTrue();
    }

    @Test
    @DisplayName("une chaine de tri vide retombe sur le tri par defaut")
    void page_triVide() {
        when(repository.findAll(any(Pageable.class))).thenReturn(pageVide());

        service.getPage(0, 10, "   ", false);

        var capture = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(capture.capture());

        assertThat(capture.getValue().getSort().getOrderFor("dateEntretien")).isNotNull();
    }

    @Test
    @DisplayName("page et taille demandees sont transmises telles quelles")
    void page_transmetPaginationDemandee() {
        when(repository.findAll(any(Pageable.class))).thenReturn(pageVide());

        service.getPage(2, 25, null, false);

        var capture = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(capture.capture());

        assertThat(capture.getValue().getPageNumber()).isEqualTo(2);
        assertThat(capture.getValue().getPageSize()).isEqualTo(25);
    }
}
