package org.autostock.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

/**
 * Canal d'envoi AWS SES (ou LocalStack en dev). Activé quand aws.ses.from est défini.
 *
 * <p>Tant que le compte AWS est dans le bac à sable, SES rejette tout envoi dont
 * l'expéditeur ou le destinataire n'est pas une identité vérifiée. C'est pourquoi
 * {@link MailDispatcher} ne l'utilise qu'en secours de SMTP.
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

    public void sendWelcomePassword(String to, String nom, String password) {
        sendHtml(to, MailTemplates.SUJET_BIENVENUE, MailTemplates.bienvenue(nom, password));
    }

    public void sendPasswordResetCode(String to, String code) {
        sendHtml(to, MailTemplates.SUJET_RESET, MailTemplates.codeReset(code));
    }

    public void sendVenteConfirmation(String to, String nomClient, String modele, String montant) {
        sendHtml(to, MailTemplates.SUJET_VENTE, MailTemplates.vente(nomClient, modele, montant));
    }

    public void sendAlertStockBas(String to, String modele, int quantite) {
        sendHtml(to, "⚠️ Alerte stock bas – Auto Stock", MailTemplates.alerteStockBas(modele, quantite));
    }

    /** Confirmation de réservation pour le client du portail Ted Auto. */
    public void sendReservationConfirmation(String to, String nomClient, String voitureLabel, LocalDate dateVisite) {
        sendHtml(to, MailTemplates.SUJET_RESERVATION,
                MailTemplates.reservation(nomClient, voitureLabel, dateVisite));
    }

    /** Email de contact reçu depuis le formulaire Ted Auto. */
    public void sendContactMessage(String adminEmail, String nomContact, String emailContact,
                                   String telephone, String sujet, String message) {
        sendHtml(adminEmail, "Contact Ted Auto : " + sujet,
                MailTemplates.contact(nomContact, emailContact, telephone, sujet, message));
    }

    // -------------------------------------------------------------------------

    /** Envoi générique. Lève une exception si SES refuse le message. */
    public void sendHtml(String to, String subject, String htmlBody) {
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
}
