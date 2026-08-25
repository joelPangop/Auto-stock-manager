package org.autostock.backup;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Export des donnees applicatives, reserve au super administrateur.
 *
 * <p>Cet endpoint produit une archive ZIP contenant le dump SQL, les fichiers
 * uploades et, optionnellement, les objets S3. Il n'inclut <strong>jamais</strong>
 * les fichiers de configuration : ceux-ci contiennent des secrets en clair et
 * n'ont pas a transiter par une reponse HTTP. Pour un export incluant la
 * configuration, utiliser le script backup.sh via SSH.
 *
 * <p>Trois garde-fous sont en place :
 * <ul>
 *   <li>autorisation stricte sur le role SUPER_ADMIN ;</li>
 *   <li>un intervalle minimum entre deux exports, pour qu'un jeton vole ne
 *       permette pas d'aspirer la base en boucle ;</li>
 *   <li>une trace d'audit systematique (utilisateur, IP, horodatage).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/admin/backup")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class BackupController {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT.backup");
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final BackupService backupService;

    /** Intervalle minimum entre deux exports. 0 desactive la limitation. */
    private final Duration minInterval;

    private final AtomicReference<Instant> lastExport = new AtomicReference<>(Instant.EPOCH);

    public BackupController(BackupService backupService,
                            @Value("${app.backup.min-interval-seconds:300}") long minIntervalSeconds) {
        this.backupService = backupService;
        this.minInterval = Duration.ofSeconds(minIntervalSeconds);
    }

    /**
     * Genere et telecharge un export complet.
     *
     * @param includeUploads inclure le repertoire des fichiers uploades
     * @param includeS3      inclure les objets des buckets S3 / LocalStack
     */
    @GetMapping(value = "/export", produces = "application/zip")
    public ResponseEntity<StreamingResponseBody> export(
            @RequestParam(defaultValue = "true") boolean includeUploads,
            @RequestParam(defaultValue = "true") boolean includeS3,
            HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = auth != null ? auth.getName() : "inconnu";
        String ip = clientIp(request);

        if (!allowNow()) {
            AUDIT.warn("EXPORT REFUSÉ (trop fréquent) — utilisateur={} ip={}", user, ip);
            return ResponseEntity.status(429)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(minInterval.toSeconds()))
                    .build();
        }

        String filename = "autostock-export-" + STAMP.format(Instant.now()) + ".zip";

        AUDIT.warn("EXPORT DÉMARRÉ — utilisateur={} ip={} fichier={} uploads={} s3={}",
                user, ip, filename, includeUploads, includeS3);

        StreamingResponseBody body = out -> {
            try {
                backupService.writeArchive(out, includeUploads, includeS3);
                AUDIT.warn("EXPORT TERMINÉ — utilisateur={} fichier={}", user, filename);
            } catch (Exception e) {
                // Le statut 200 est deja parti : on ne peut plus changer la reponse.
                // La trace d'audit est donc le seul endroit ou l'echec reste visible,
                // et l'archive tronquee sera rejetee par la verification d'integrite.
                AUDIT.error("EXPORT ÉCHOUÉ — utilisateur={} fichier={} : {}",
                        user, filename, e.getMessage(), e);
                throw new java.io.IOException("Export interrompu", e);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(body);
    }

    /** Indique si un export est autorise maintenant, et reserve le creneau si oui. */
    private boolean allowNow() {
        if (minInterval.isZero() || minInterval.isNegative()) {
            return true;
        }
        Instant now = Instant.now();
        Instant previous = lastExport.get();
        if (Duration.between(previous, now).compareTo(minInterval) < 0) {
            return false;
        }
        // compareAndSet : deux requetes simultanees ne peuvent pas passer toutes les deux.
        return lastExport.compareAndSet(previous, now);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // nginx ajoute l'IP reelle en tete de liste.
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
