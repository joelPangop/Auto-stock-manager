package org.autostock.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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

    public void sendPasswordResetCode(String to, String code) {
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
            helper.setSubject("Réinitialisation de mot de passe – Auto Stock");
            helper.setText(buildHtml(code), true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new RuntimeException("Échec d'envoi de l'email : " + e.getMessage(), e);
        }
    }

    private String buildHtml(String code) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px">
                  <h2 style="color:#1e3a8a;margin-bottom:8px">Réinitialisation de mot de passe</h2>
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
}
