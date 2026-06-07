package org.autostock.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

/**
 * Service d'envoi d'email via AWS SES (ou LocalStack en dev).
 * Activé quand aws.ses.from est défini.
 * Remplace / complète EmailService (SMTP Gmail).
 */
@Service
@ConditionalOnProperty(name = "aws.ses.from")
@Slf4j
public class SesEmailService {

    private final SesClient ses;

    @Value("${aws.ses.from}")
    private String from;

    public SesEmailService(SesClient ses) {
        this.ses = ses;
    }

    public void sendPasswordResetCode(String to, String code) {
        send(to, "Réinitialisation de mot de passe – Auto Stock", buildResetHtml(code));
    }

    public void sendVenteConfirmation(String to, String nomClient, String modele, String montant) {
        send(to, "Confirmation d'achat – Auto Stock",
                buildVenteHtml(nomClient, modele, montant));
    }

    public void sendAlertStockBas(String to, String modele, int quantite) {
        send(to, "⚠️ Alerte stock bas – Auto Stock",
                buildStockAlertHtml(modele, quantite));
    }

    // -------------------------------------------------------------------------

    private void send(String to, String subject, String htmlBody) {
        try {
            ses.sendEmail(SendEmailRequest.builder()
                    .source(from)
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                    .build())
                            .build())
                    .build());
            log.info("[SES] Email envoyé à {} — sujet : {}", to, subject);
        } catch (SesException e) {
            throw new RuntimeException("Échec d'envoi SES : " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Templates HTML
    // -------------------------------------------------------------------------

    private String buildResetHtml(String code) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px">
                  <h2 style="color:#1e3a8a">Réinitialisation de mot de passe</h2>
                  <p style="color:#374151">Votre code de réinitialisation est :</p>
                  <div style="font-size:36px;font-weight:bold;letter-spacing:10px;text-align:center;
                              padding:20px;background:#f1f5f9;border-radius:10px;color:#1e3a8a;margin:20px 0">
                    %s
                  </div>
                  <p style="color:#6b7280;font-size:14px">Ce code expire dans <strong>15 minutes</strong>.</p>
                  <p style="color:#9ca3af;font-size:12px">Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.</p>
                </div>
                """.formatted(code);
    }

    private String buildVenteHtml(String nomClient, String modele, String montant) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px">
                  <h2 style="color:#1e3a8a">Confirmation d'achat</h2>
                  <p style="color:#374151">Bonjour <strong>%s</strong>,</p>
                  <p>Votre achat du véhicule <strong>%s</strong> pour un montant de <strong>%s FCFA</strong> a bien été enregistré.</p>
                  <p style="color:#6b7280;font-size:14px">Merci de votre confiance – Auto Stock</p>
                </div>
                """.formatted(nomClient, modele, montant);
    }

    private String buildStockAlertHtml(String modele, int quantite) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px">
                  <h2 style="color:#dc2626">⚠️ Alerte stock bas</h2>
                  <p>Le stock du modèle <strong>%s</strong> est bas : <strong>%d</strong> unité(s) restante(s).</p>
                  <p style="color:#6b7280;font-size:14px">Veuillez réapprovisionner dès que possible.</p>
                </div>
                """.formatted(modele, quantite);
    }
}
