package org.autostock.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

/**
 * Service de stockage des fichiers sur AWS S3 (ou LocalStack en dev).
 * Activé quand aws.s3.bucket est défini.
 */
@Service
@ConditionalOnProperty(name = "aws.s3.bucket")
@Slf4j
public class S3StorageService {

    private final S3Client s3;
    private final S3Presigner presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.endpoint-override:}")
    private String endpointOverride;

    public S3StorageService(S3Client s3, S3Presigner presigner) {
        this.s3 = s3;
        this.presigner = presigner;
    }

    /**
     * Upload un fichier dans S3 et retourne sa clé (key).
     */
    public String upload(MultipartFile file, String folder) {
        String key = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            log.info("[S3] Fichier uploadé : s3://{}/{}", bucket, key);
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Erreur upload S3 : " + e.getMessage(), e);
        }
    }

    /**
     * Upload des bytes bruts (ex: PDF généré).
     */
    public String uploadBytes(byte[] data, String key, String contentType) {
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .contentLength((long) data.length)
                        .build(),
                RequestBody.fromBytes(data)
        );
        log.info("[S3] Bytes uploadés : s3://{}/{}", bucket, key);
        return key;
    }

    /**
     * Génère une URL pré-signée valable 1 heure.
     * En développement LocalStack, retourne une URL publique directe.
     */
    public String generatePresignedUrl(String key) {
        if (endpointOverride != null && !endpointOverride.isBlank()) {
            // LocalStack : URL publique directe
            return endpointOverride + "/" + bucket + "/" + key;
        }
        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(r -> r.bucket(bucket).key(key))
                .build();
        return presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * Télécharge un fichier depuis S3 sous forme de bytes.
     */
    public byte[] download(String key) {
        return s3.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(key).build()
        ).asByteArray();
    }

    /**
     * Supprime un fichier de S3.
     */
    public void delete(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        log.info("[S3] Fichier supprimé : s3://{}/{}", bucket, key);
    }

    /**
     * Retourne le nom de fichier à partir d'une clé S3.
     */
    public static String keyToFilename(String key) {
        if (key == null) return null;
        int slash = key.lastIndexOf('/');
        return slash >= 0 ? key.substring(slash + 1) : key;
    }
}
