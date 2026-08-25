package org.autostock.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Instant;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Construit l'archive d'export et l'ecrit directement dans le flux de reponse.
 *
 * <p>Rien n'est stocke sur disque au passage : sur une base volumineuse, ecrire
 * un fichier temporaire remplirait le conteneur. Le ZIP est genere a la volee,
 * entree par entree.
 *
 * <p>Le service ne depend d'aucune configuration nouvelle. Le dump SQL passe par
 * la {@link DataSource} de l'application, l'export S3 par le {@code S3Client}
 * existant s'il y en a un. Seul le repertoire des fichiers uploades peut avoir
 * besoin d'etre precise, et son absence n'est pas une erreur.
 */
@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);

    private final DataSource dataSource;
    private final Path uploadsDir;
    private final ObjectProvider<S3Client> s3ClientProvider;

    public BackupService(
            DataSource dataSource,
            // Reprend la propriete d'upload de l'application si elle existe,
            // sinon l'emplacement conventionnel dans l'image.
            @Value("${app.upload.dir:${app.backup.uploads-dir:/app/uploads}}") String uploadsDir,
            ObjectProvider<S3Client> s3ClientProvider) {

        this.dataSource = dataSource;
        this.uploadsDir = Paths.get(uploadsDir);
        this.s3ClientProvider = s3ClientProvider;
    }

    /**
     * Ecrit l'archive complete dans le flux fourni.
     *
     * @throws IOException si le dump SQL echoue — l'archive est alors
     *                     volontairement interrompue plutot que livree incomplete
     */
    public void writeArchive(OutputStream out, boolean includeUploads, boolean includeS3)
            throws IOException {

        try (ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            zip.setLevel(6); // compromis taille / CPU

            String schema = writeSqlDump(zip);
            int uploadCount = includeUploads ? writeUploads(zip) : 0;
            int s3Count = includeS3 ? writeS3Objects(zip) : 0;

            writeManifest(zip, schema, includeUploads, includeS3, uploadCount, s3Count);
            zip.finish();
        }
    }

    // -----------------------------------------------------------------------
    // Dump SQL
    // -----------------------------------------------------------------------

    private String writeSqlDump(ZipOutputStream zip) throws IOException {
        zip.putNextEntry(new ZipEntry("dump.sql"));
        try {
            // Writer non ferme volontairement : fermer propagerait la fermeture
            // au ZipOutputStream et couperait les entrees suivantes.
            Writer w = new OutputStreamWriter(zip, StandardCharsets.UTF_8);
            String schema = new SqlDumpWriter(dataSource).write(w);
            w.flush();
            zip.closeEntry();
            return schema;
        } catch (SQLException e) {
            throw new IOException("Échec du dump SQL : " + e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------
    // Fichiers uploades
    // -----------------------------------------------------------------------

    private int writeUploads(ZipOutputStream zip) throws IOException {
        if (!Files.isDirectory(uploadsDir)) {
            log.warn("Répertoire uploads absent, ignoré : {}", uploadsDir);
            return 0;
        }
        int count = 0;
        try (Stream<Path> files = Files.walk(uploadsDir)) {
            for (Path p : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                String name = "uploads/" + uploadsDir.relativize(p).toString().replace('\\', '/');
                zip.putNextEntry(new ZipEntry(name));
                Files.copy(p, zip);
                zip.closeEntry();
                count++;
            }
        }
        log.info("{} fichier(s) uploadé(s) ajouté(s)", count);
        return count;
    }

    // -----------------------------------------------------------------------
    // Objets S3 / LocalStack
    // -----------------------------------------------------------------------

    private int writeS3Objects(ZipOutputStream zip) throws IOException {
        S3Client s3 = s3ClientProvider.getIfAvailable();
        if (s3 == null) {
            log.warn("Aucun bean S3Client — export S3 ignoré");
            return 0;
        }

        int count = 0;
        for (Bucket bucket : s3.listBuckets().buckets()) {
            String name = bucket.name();
            String token = null;
            do {
                ListObjectsV2Request.Builder req = ListObjectsV2Request.builder().bucket(name);
                if (token != null) {
                    req.continuationToken(token);
                }
                ListObjectsV2Response resp = s3.listObjectsV2(req.build());

                for (S3Object obj : resp.contents()) {
                    // Les "repertoires" S3 sont des cles vides : rien a archiver.
                    if (obj.key().endsWith("/") && obj.size() == 0) {
                        continue;
                    }
                    zip.putNextEntry(new ZipEntry("s3/" + name + "/" + obj.key()));
                    try (InputStream in = s3.getObject(
                            GetObjectRequest.builder().bucket(name).key(obj.key()).build())) {
                        in.transferTo(zip);
                    } catch (Exception e) {
                        log.warn("Objet illisible ignoré : s3://{}/{} ({})", name, obj.key(), e.getMessage());
                    }
                    zip.closeEntry();
                    count++;
                }
                token = Boolean.TRUE.equals(resp.isTruncated()) ? resp.nextContinuationToken() : null;
            } while (token != null);
        }
        log.info("{} objet(s) S3 ajouté(s)", count);
        return count;
    }

    // -----------------------------------------------------------------------
    // Manifeste
    // -----------------------------------------------------------------------

    private void writeManifest(ZipOutputStream zip, String schema, boolean uploads, boolean s3,
                               int uploadCount, int s3Count) throws IOException {
        String json = """
                {
                  "created_at": "%s",
                  "source": "http-endpoint",
                  "database": "%s",
                  "includes_uploads": %s,
                  "includes_s3": %s,
                  "includes_config": false,
                  "uploads_files": %d,
                  "s3_files": %d
                }
                """.formatted(Instant.now(), schema, uploads, s3, uploadCount, s3Count);

        zip.putNextEntry(new ZipEntry("manifest.json"));
        zip.write(json.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
