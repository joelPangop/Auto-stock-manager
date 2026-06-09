package org.autostock.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autostock.repositories.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Synchronise automatiquement les fichiers du disque local vers S3 au démarrage.
 *
 * Logique :
 * 1. Parcourt tous les fichiers dans le répertoire uploads/
 * 2. Pour chaque fichier, vérifie s'il existe déjà dans S3 (via la DB)
 * 3. Si absent de S3, l'upload
 *
 * Ce service est optionnel (ne bloque pas le démarrage si S3 est indisponible).
 */
@Component
@ConditionalOnBean(S3StorageService.class)
@RequiredArgsConstructor
@Slf4j
public class S3SyncService {

    private final S3StorageService s3Storage;
    private final DocumentRepository documentRepository;

    @Value("${autostock.docs.dir:uploads}")
    private String uploadsDir;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void syncDiskToS3OnStartup() {
        log.info("[S3Sync] Démarrage de la synchronisation disque → S3 (bucket: {})", s3Storage.getBucket());

        Path root = Path.of(uploadsDir).toAbsolutePath().normalize();
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            log.warn("[S3Sync] Répertoire uploads introuvable : {}", root);
            return;
        }

        // Récupérer toutes les clés S3 connues depuis la DB
        List<String> s3Keys = documentRepository.findAll().stream()
                .map(d -> d.getUrlFichier())
                .filter(url -> url != null && !url.startsWith("/api/"))
                .toList();

        AtomicInteger uploaded = new AtomicInteger(0);
        AtomicInteger skipped  = new AtomicInteger(0);
        AtomicInteger errors   = new AtomicInteger(0);

        try (var stream = Files.list(root)) {
            stream.filter(Files::isRegularFile).forEach(file -> {
                String filename = file.getFileName().toString();

                // Trouver le document correspondant dans la DB
                documentRepository.findAll().stream()
                    .filter(d -> filename.equals(d.getNomFichier()))
                    .findFirst()
                    .ifPresentOrElse(doc -> {
                        String existingKey = doc.getUrlFichier();

                        // Déjà dans S3 ?
                        if (existingKey != null && !existingKey.startsWith("/api/")) {
                            try {
                                if (s3Storage.exists(existingKey)) {
                                    skipped.incrementAndGet();
                                    return;
                                }
                            } catch (Exception e) {
                                // Continuer et ré-uploader
                            }
                        }

                        // Déterminer le dossier S3 depuis la voiture
                        String folder = "voitures/" + (doc.getVoiture() != null ? doc.getVoiture().getId() : "orphan");
                        try {
                            String key = s3Storage.uploadFromPath(file, folder, filename);
                            doc.setUrlFichier(key);
                            documentRepository.save(doc);
                            uploaded.incrementAndGet();
                            log.info("[S3Sync] Uploadé : {}", key);
                        } catch (Exception e) {
                            errors.incrementAndGet();
                            log.error("[S3Sync] Erreur upload {} : {}", filename, e.getMessage());
                        }
                    }, () -> {
                        // Fichier sur disque sans entrée DB — on ignore
                        log.debug("[S3Sync] Fichier sans entrée DB ignoré : {}", filename);
                    });
            });
        } catch (IOException e) {
            log.error("[S3Sync] Erreur lecture répertoire uploads : {}", e.getMessage());
            return;
        }

        log.info("[S3Sync] Synchronisation terminée — uploadés: {}, déjà présents: {}, erreurs: {}",
                uploaded.get(), skipped.get(), errors.get());
    }
}
