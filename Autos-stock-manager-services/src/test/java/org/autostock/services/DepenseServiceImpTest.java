package org.autostock.services;

import org.autostock.dtos.DepenseCreateDto;
import org.autostock.dtos.DepenseDto;
import org.autostock.mappers.DepenseMapper;
import org.autostock.models.Depense;
import org.autostock.models.Document;
import org.autostock.models.Entretien;
import org.autostock.models.Fournisseur;
import org.autostock.models.Voiture;
import org.autostock.repositories.DepenseRepository;
import org.autostock.repositories.DocumentRepository;
import org.autostock.repositories.EntretienRepository;
import org.autostock.repositories.FournisseurRepository;
import org.autostock.repositories.VoitureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Depenses rattachees a un vehicule.
 *
 * <p>Le point le plus important est le forcage de voitureId depuis le chemin de
 * l'URL : sans lui, un client pourrait imputer une depense a un vehicule qui
 * n'est pas le sien en manipulant le corps de la requete. Les autres tests
 * verrouillent le fait qu'une reference cassee (entretien, document ou
 * fournisseur inexistant) fasse echouer la creation au lieu de produire une
 * depense silencieusement orpheline.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DepenseServiceImpTest {

    @Mock private DepenseRepository repository;
    @Mock private VoitureRepository voitureRepository;
    @Mock private EntretienRepository entretienRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private FournisseurRepository fournisseurRepository;
    @Mock private DepenseMapper depenseMapper;

    private DepenseServiceImp service;

    private Voiture voiture;

    @BeforeEach
    void setUp() {
        service = new DepenseServiceImp();
        ReflectionTestUtils.setField(service, "repository", repository);
        ReflectionTestUtils.setField(service, "voitureRepository", voitureRepository);
        ReflectionTestUtils.setField(service, "entretienRepository", entretienRepository);
        ReflectionTestUtils.setField(service, "documentRepository", documentRepository);
        ReflectionTestUtils.setField(service, "fournisseurRepository", fournisseurRepository);
        ReflectionTestUtils.setField(service, "depenseMapper", depenseMapper);

        voiture = new Voiture();
        voiture.setId(1L);

        when(voitureRepository.findById(1L)).thenReturn(Optional.of(voiture));
        when(depenseMapper.toEntity(any(), any(), any(), any(), any())).thenReturn(new Depense());
        when(repository.save(any(Depense.class))).thenAnswer(inv -> inv.getArgument(0));
        when(depenseMapper.toDto(any(Depense.class))).thenReturn(new DepenseDto());
    }

    private DepenseCreateDto demande() {
        return new DepenseCreateDto();
    }

    // =====================================================================

    @Test
    @DisplayName("la voiture du chemin ecrase celle du corps de la requete")
    void create_forceLaVoitureDuChemin() {
        var dto = demande();
        dto.setVoitureId(999L); // tentative d imputation sur un autre vehicule

        service.create(1L, dto);

        assertThat(dto.getVoitureId())
                .as("sans ce forcage, on pourrait imputer une depense au vehicule d autrui")
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("une depense sur une voiture inexistante est refusee")
    void create_voitureInexistante() {
        when(voitureRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(404L, demande()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Voiture introuvable");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("sans reference optionnelle, la depense est creee")
    void create_sansReferences() {
        assertThat(service.create(1L, demande())).isNotNull();

        verify(repository).save(any(Depense.class));
    }

    @Test
    @DisplayName("un entretien reference est resolu et transmis au mapper")
    void create_resoutLEntretien() {
        var entretien = new Entretien();
        entretien.setId(5L);
        when(entretienRepository.findById(5L)).thenReturn(Optional.of(entretien));

        var dto = demande();
        dto.setEntretienId(5L);

        service.create(1L, dto);

        verify(depenseMapper).toEntity(any(), any(), org.mockito.ArgumentMatchers.eq(entretien),
                any(), any());
    }

    @Test
    @DisplayName("un entretien inexistant fait echouer la creation")
    void create_entretienInexistant() {
        when(entretienRepository.findById(404L)).thenReturn(Optional.empty());

        var dto = demande();
        dto.setEntretienId(404L);

        assertThatThrownBy(() -> service.create(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Entretien introuvable");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("un document inexistant fait echouer la creation")
    void create_documentInexistant() {
        when(documentRepository.findById(404L)).thenReturn(Optional.empty());

        var dto = demande();
        dto.setDocumentId(404L);

        assertThatThrownBy(() -> service.create(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Document introuvable");
    }

    @Test
    @DisplayName("un fournisseur inexistant fait echouer la creation")
    void create_fournisseurInexistant() {
        when(fournisseurRepository.findById(404L)).thenReturn(Optional.empty());

        var dto = demande();
        dto.setFournisseurId(404L);

        assertThatThrownBy(() -> service.create(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fournisseur introuvable");
    }

    @Test
    @DisplayName("un fournisseur reference est resolu et transmis au mapper")
    void create_resoutLeFournisseur() {
        var fournisseur = new Fournisseur();
        fournisseur.setId(9L);
        when(fournisseurRepository.findById(9L)).thenReturn(Optional.of(fournisseur));

        var dto = demande();
        dto.setFournisseurId(9L);

        service.create(1L, dto);

        verify(depenseMapper).toEntity(any(), any(), any(), any(),
                org.mockito.ArgumentMatchers.eq(fournisseur));
    }

    @Test
    @DisplayName("un document reference est resolu et transmis au mapper")
    void create_resoutLeDocument() {
        var document = new Document();
        document.setId(7L);
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));

        var dto = demande();
        dto.setDocumentId(7L);

        service.create(1L, dto);

        verify(depenseMapper).toEntity(any(), any(), any(),
                org.mockito.ArgumentMatchers.eq(document), any());
    }

    @Test
    @DisplayName("la voiture resolue est celle transmise au mapper")
    void create_transmetLaVoitureResolue() {
        service.create(1L, demande());

        verify(depenseMapper).toEntity(any(), org.mockito.ArgumentMatchers.eq(voiture),
                any(), any(), any());
    }
}
