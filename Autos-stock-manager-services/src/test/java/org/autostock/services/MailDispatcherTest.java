package org.autostock.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Ordre des canaux d'envoi et tolerance a la panne.
 *
 * <p>SMTP passe en premier : il accepte n'importe quel destinataire, alors que
 * SES en bac a sable refuse toute adresse non verifiee. L'ordre inverse est
 * precisement le defaut qui empechait les invitations de partir.
 */
@ExtendWith(MockitoExtension.class)
class MailDispatcherTest {

    @Mock private EmailService smtp;
    @Mock private SesEmailService ses;

    private MailDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new MailDispatcher(smtp);
        ReflectionTestUtils.setField(dispatcher, "ses", ses);
    }

    @Test
    @DisplayName("SMTP est essaye en premier et SES n'est pas sollicite s'il aboutit")
    void smtpDAbord() {
        boolean envoye = dispatcher.send("dest@test.fr", "Sujet", "<p>corps</p>");

        assertThat(envoye).isTrue();
        verify(smtp).sendHtml("dest@test.fr", "Sujet", "<p>corps</p>");
        verify(ses, never()).sendHtml(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("SES prend le relais quand SMTP echoue")
    void sesEnSecours() {
        doThrow(new RuntimeException("SMTP indisponible"))
                .when(smtp).sendHtml(anyString(), anyString(), anyString());

        boolean envoye = dispatcher.send("dest@test.fr", "Sujet", "<p>corps</p>");

        assertThat(envoye).isTrue();
        verify(ses).sendHtml("dest@test.fr", "Sujet", "<p>corps</p>");
    }

    @Test
    @DisplayName("les deux canaux en echec renvoient false sans lever d'exception")
    void echecTotalNeLevePas() {
        doThrow(new RuntimeException("SMTP KO"))
                .when(smtp).sendHtml(anyString(), anyString(), anyString());
        doThrow(new RuntimeException("SES KO"))
                .when(ses).sendHtml(anyString(), anyString(), anyString());

        boolean envoye = dispatcher.send("dest@test.fr", "Sujet", "<p>corps</p>");

        assertThat(envoye)
                .as("un email rate ne doit jamais faire echouer l'operation metier, "
                        + "mais l'appelant doit pouvoir en informer l'utilisateur")
                .isFalse();
    }

    @Test
    @DisplayName("sans bean SES, l'echec SMTP renvoie false sans NullPointerException")
    void sansSes() {
        ReflectionTestUtils.setField(dispatcher, "ses", null);
        doThrow(new RuntimeException("SMTP KO"))
                .when(smtp).sendHtml(anyString(), anyString(), anyString());

        assertThat(dispatcher.send("dest@test.fr", "Sujet", "<p>corps</p>")).isFalse();
    }
}
