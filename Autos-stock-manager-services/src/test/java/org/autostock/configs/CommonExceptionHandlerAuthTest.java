package org.autostock.configs;

import org.autostock.dtos.ApiError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Un echec d'authentification est une erreur du client, pas du serveur.
 * Les exceptions de Spring Security heritent de RuntimeException : sans
 * handler dedie, le fourre-tout les renvoyait en 500.
 */
class CommonExceptionHandlerAuthTest {

    private final CommonExceptionHandler handler = new CommonExceptionHandler();

    @Test
    @DisplayName("des identifiants faux renvoient 401, pas 500")
    void identifiantsFauxEn401() {
        ResponseEntity<ApiError> res = handler.authenticationFailed(new BadCredentialsException("Bad credentials"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("le message ne revele pas si l email existe")
    void messageNeutre() {
        ResponseEntity<ApiError> res = handler.authenticationFailed(new BadCredentialsException("Bad credentials"));

        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().message()).isEqualTo("Email ou mot de passe incorrect.");
    }

    @Test
    @DisplayName("les autres refus d authentification gardent leur motif")
    void autresRefusConserventLeurMotif() {
        ResponseEntity<ApiError> res = handler.authenticationFailed(new DisabledException("Compte desactive"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody().message()).isEqualTo("Compte desactive");
    }

    @Test
    @DisplayName("une vraie erreur interne reste en 500")
    void erreurInterneResteEn500() {
        ResponseEntity<ApiError> res = handler.runtimeException(new RuntimeException("panne"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
