package org.autostock.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Point d'envoi unique des emails applicatifs.
 *
 * <p>SMTP est le canal principal : il accepte n'importe quel destinataire.
 * SES ne sert que de secours car tant que le compte AWS est dans le bac à
 * sable, il refuse toute adresse non vérifiée — il ne peut donc pas porter les
 * invitations ni les confirmations envoyées à des clients.
 *
 * <p>Aucune méthode ne propage d'exception : un email raté ne doit jamais faire
 * échouer l'opération métier qui l'a déclenché. L'issue réelle est retournée
 * pour que l'appelant puisse en informer l'utilisateur au lieu d'annoncer un
 * succès qui n'a pas eu lieu.
 */
@Service
@Slf4j
public class MailDispatcher {

    private final EmailService smtp;

    @Autowired(required = false)
    private SesEmailService ses;

    public MailDispatcher(EmailService smtp) {
        this.smtp = smtp;
    }

    /**
     * @return true si l'email est effectivement parti par l'un des canaux
     */
    public boolean send(String to, String subject, String htmlBody) {
        try {
            smtp.sendHtml(to, subject, htmlBody);
            log.info("[Mail] Envoyé à {} via SMTP — {}", to, subject);
            return true;
        } catch (Exception e) {
            log.warn("[Mail] SMTP a échoué pour {} : {}{}", to, e.getMessage(),
                    ses != null ? " — tentative via SES" : "");
        }

        if (ses != null) {
            try {
                ses.sendHtml(to, subject, htmlBody);
                log.info("[Mail] Envoyé à {} via SES — {}", to, subject);
                return true;
            } catch (Exception e) {
                log.error("[Mail] Aucun canal n'a pu envoyer à {} : {}", to, e.getMessage());
                return false;
            }
        }

        log.error("[Mail] Aucun canal disponible pour {}", to);
        return false;
    }
}
