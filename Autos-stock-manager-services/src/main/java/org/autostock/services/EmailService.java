package org.autostock.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Canal d'envoi SMTP (relais Gmail). Accepte n'importe quel destinataire,
 * contrairement à {@link SesEmailService} en bac à sable.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${spring.mail.password:}")
    private String password;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private boolean isConfigured() {
        return !from.isBlank()
                && !password.isBlank()
                && from.contains("@")
                && !from.contains("votre.email")
                && !password.contains("xxxx");
    }

    /** Envoi générique. Lève une exception si le canal est inutilisable. */
    public void sendHtml(String to, String subject, String htmlBody) {
        if (!isConfigured()) {
            throw new UnsupportedOperationException(
                    "Service email non configuré. Renseignez MAIL_USERNAME et MAIL_PASSWORD dans le fichier .env"
            );
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new RuntimeException("Échec d'envoi de l'email : " + e.getMessage(), e);
        }
    }

    public void sendWelcomePassword(String to, String nom, String password) {
        sendHtml(to, MailTemplates.SUJET_BIENVENUE, MailTemplates.bienvenue(nom, password));
    }

    public void sendPasswordResetCode(String to, String code) {
        sendHtml(to, MailTemplates.SUJET_RESET, MailTemplates.codeReset(code));
    }
}
