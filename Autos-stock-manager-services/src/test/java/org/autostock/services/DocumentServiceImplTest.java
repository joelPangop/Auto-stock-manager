package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.dtos.DocumentDto;
import org.autostock.mappers.DocumentMapper;
import org.autostock.models.Depense;
import org.autostock.models.Document;
import org.autostock.models.Voiture;
import org.autostock.repositories.DepenseRepository;
import org.autostock.repositories.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Documents et photos rattaches a un vehicule.
 *
 * <p>Deux comportements meritent d'etre figes. La photo principale doit rester
 * unique par vehicule, sinon l'affichage du catalogue devient indeterministe.
 * Et la suppression doit d'abord dissocier les depenses qui referencent le
 * document : une depense appartient au vehicule, pas au justificatif, et la
 * supprimer en cascade ferait disparaitre une ecriture comptable.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentServiceImplTest {

    @Mock private DocumentRepository repository;
    @Mock private DepenseRepository depenseRepository;
    @Mock private DocumentMapper documentMapper;
    @Mock private S3StorageService s3Storage;

    private DocumentServiceImpl service;

    private Voiture voiture;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        service = new DocumentServiceImpl(tempDir.toString());
        ReflectionTestUtils.setField(service, "repository", repository);
        ReflectionTestUtils.setField(service, "depenseRepository", depenseRepository);
        ReflectionTestUtils.setField(service, "documentMapper", documentMapper);

        voiture = new Voiture();
        voiture.setId(1L);

        when(repository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        when(documentMapper.toDto(any(Document.class))).thenReturn(new DocumentDto());
        when(depenseRepository.findAllByDocument_Id(any())).thenReturn(List.of());
    }

    private Document document(Long id, boolean principale) {
        var d = new Document();
        d.setId(id);
        d.setVoiture(voiture);
        d.setPrincipale(principale);
        d.setNomFichier("fichier-" + id + ".jpg");
        return d;
    }

    // =====================================================================
    // Photo principale
    // =====================================================================

    @Test
    @DisplayName("designer une photo principale la marque comme telle")
    void photoPrincipale_marqueLeDocument() {
        var doc = document(10L, false);
        when(repository.findById(10L)).thenReturn(Optional.of(doc));
        when(repository.findByVoiture_IdAndPrincipaleTrue(1L)).thenReturn(List.of());

        service.setPhotoPrincipale(10L);

        assertThat(doc.isPrincipale()).isTrue();
    }

    @Test
    @DisplayName("l ancienne photo principale du meme vehicule perd son drapeau")
    void photoPrincipale_retireLAncienne() {
        var ancienne = document(9L, true);
        var nouvelle = document(10L, false);
        when(repository.findById(10L)).thenReturn(Optional.of(nouvelle));
        when(repository.findByVoiture_IdAndPrincipaleTrue(1L)).thenReturn(List.of(ancienne));

        service.setPhotoPrincipale(10L);

        assertThat(ancienne.isPrincipale())
                .as("deux photos principales rendraient l affichage du catalogue indeterministe")
                .isFalse();
        assertThat(nouvelle.isPrincipale()).isTrue();
    }

    @Test
    @DisplayName("plusieurs anciennes principales sont toutes remises a plat")
    void photoPrincipale_nettoieToutesLesAnciennes() {
        var a = document(7L, true);
        var b = document(8L, true);
        var nouvelle = document(10L, false);
        when(repository.findById(10L)).thenReturn(Optional.of(nouvelle));
        when(repository.findByVoiture_IdAndPrincipaleTrue(1L)).thenReturn(List.of(a, b));

        service.setPhotoPrincipale(10L);

        assertThat(a.isPrincipale()).isFalse();
        assertThat(b.isPrincipale()).isFalse();
    }

    @Test
    @DisplayName("designer un document inexistant echoue")
    void photoPrincipale_documentInexistant() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setPhotoPrincipale(404L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Document introuvable");
    }

    // =====================================================================
    // Suppression
    // =====================================================================

    @Test
    @DisplayName("les depenses liees sont dissociees, jamais supprimees")
    void suppression_dissocieLesDepenses() {
        var doc = document(10L, false);
        when(repository.findById(10L)).thenReturn(Optional.of(doc));

        var depense = new Depense();
        depense.setDocument(doc);
        when(depenseRepository.findAllByDocument_Id(10L)).thenReturn(List.of(depense));

        service.delete(10L);

        assertThat(depense.getDocument())
                .as("une depense appartient au vehicule, pas au justificatif : "
                        + "la supprimer en cascade ferait disparaitre une ecriture comptable")
                .isNull();
        verify(depenseRepository).saveAll(anyList());
        verify(depenseRepository, never()).delete(any());
    }

    @Test
    @DisplayName("sans depense liee, aucune ecriture inutile")
    void suppression_sansDepenseLiee() {
        var doc = document(10L, false);
        when(repository.findById(10L)).thenReturn(Optional.of(doc));
        when(depenseRepository.findAllByDocument_Id(10L)).thenReturn(List.of());

        service.delete(10L);

        verify(depenseRepository, never()).saveAll(anyList());
        verify(repository).delete(doc);
    }

    @Test
    @DisplayName("le document est bien supprime a la fin")
    void suppression_supprimeLeDocument() {
        var doc = document(10L, false);
        when(repository.findById(10L)).thenReturn(Optional.of(doc));

        service.delete(10L);

        verify(repository).delete(doc);
    }

    @Test
    @DisplayName("le fichier S3 est supprime quand un stockage S3 est actif")
    void suppression_supprimeLeFichierS3() {
        ReflectionTestUtils.setField(service, "s3Storage", s3Storage);

        var doc = document(10L, false);
        doc.setUrlFichier("s3://bucket/cle.jpg");
        when(repository.findById(10L)).thenReturn(Optional.of(doc));

        service.delete(10L);

        verify(s3Storage).delete("s3://bucket/cle.jpg");
    }

    @Test
    @DisplayName("un echec de suppression S3 ne bloque pas la suppression en base")
    void suppression_survitAUnEchecS3() {
        ReflectionTestUtils.setField(service, "s3Storage", s3Storage);
        org.mockito.Mockito.doThrow(new RuntimeException("S3 indisponible"))
                .when(s3Storage).delete(any());

        var doc = document(10L, false);
        doc.setUrlFichier("s3://bucket/cle.jpg");
        when(repository.findById(10L)).thenReturn(Optional.of(doc));

        service.delete(10L);

        verify(repository)
                .delete(doc);
    }

    @Test
    @DisplayName("supprimer un document inexistant echoue")
    void suppression_documentInexistant() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(404L))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }
}
