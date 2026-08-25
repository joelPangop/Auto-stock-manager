package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.dtos.auth.AdminCreateUserRequest;
import org.autostock.dtos.auth.ChangePasswordRequest;
import org.autostock.dtos.auth.LoginRequest;
import org.autostock.enums.Role;
import org.autostock.models.User;
import org.autostock.repositories.PasswordResetTokenRepository;
import org.autostock.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cycle de vie du mot de passe : creation par un admin, connexion, changement
 * par l'utilisateur, regeneration, expiration.
 *
 * <p>Le PasswordEncoder n'est volontairement pas mocke : c'est un vrai
 * BCryptPasswordEncoder. Mocker le hachage rendrait les assertions creuses —
 * on veut verifier qu'un mot de passe donne correspond reellement au hash
 * enregistre, pas qu'une methode a ete appelee.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock private UserRepository repo;
    @Mock private PasswordResetTokenRepository tokenRepo;
    @Mock private EmailService emailService;
    @Mock private MailDispatcher mailDispatcher;
    @Mock private SmsService smsService;
    @Mock private AuthenticationManager authManager;
    @Mock private JwtService jwt;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(encoder, authManager, jwt);
        ReflectionTestUtils.setField(authService, "repo", repo);
        ReflectionTestUtils.setField(authService, "tokenRepo", tokenRepo);
        ReflectionTestUtils.setField(authService, "emailService", emailService);
        ReflectionTestUtils.setField(authService, "mailDispatcher", mailDispatcher);
        ReflectionTestUtils.setField(authService, "smsService", smsService);

        when(jwt.generateAccessToken(any())).thenReturn("access-token");
        when(jwt.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwt.getAccessExpirationMs()).thenReturn(3_600_000L);
    }

    private User compte(String email, String motDePasse, Role role) {
        return User.builder()
                .nom("Test")
                .email(email)
                .motDePasseHash(encoder.encode(motDePasse))
                .role(role)
                .accountLocked(false)
                .build();
    }

    // =====================================================================
    @Nested
    @DisplayName("Creation par un administrateur")
    class CreationParAdmin {

        @Test
        @DisplayName("genere un mot de passe temporaire valable 14 jours et l'envoie par email")
        void creation_envoieUnMotDePasseTemporaire() {
            when(repo.findByEmail("nouveau@test.fr")).thenReturn(Optional.empty());
            when(mailDispatcher.send(anyString(), anyString(), anyString())).thenReturn(true);

            var req = new AdminCreateUserRequest("Nouveau", "nouveau@test.fr", "0600000000", "USER");
            boolean envoye = authService.createUserByAdmin(req, "ADMIN");

            assertThat(envoye).isTrue();

            var capture = ArgumentCaptor.forClass(User.class);
            verify(repo).save(capture.capture());
            User cree = capture.getValue();

            assertThat(cree.getRole()).isEqualTo(Role.USER);
            assertThat(cree.isAccountLocked()).isFalse();
            assertThat(cree.getPasswordExpiresAt())
                    .as("le mot de passe temporaire expire dans ~14 jours")
                    .isBetween(LocalDateTime.now().plusDays(13), LocalDateTime.now().plusDays(15));
            assertThat(cree.getPasswordChangedAt())
                    .as("horodatage necessaire a la revocation des jetons")
                    .isNotNull();
        }

        @Test
        @DisplayName("le mot de passe temporaire n'est jamais stocke en clair")
        void creation_neStockePasLeMotDePasseEnClair() {
            when(repo.findByEmail(anyString())).thenReturn(Optional.empty());
            when(mailDispatcher.send(anyString(), anyString(), anyString())).thenReturn(true);

            authService.createUserByAdmin(
                    new AdminCreateUserRequest("N", "n@test.fr", null, "USER"), "ADMIN");

            var capture = ArgumentCaptor.forClass(User.class);
            verify(repo).save(capture.capture());
            assertThat(capture.getValue().getMotDePasseHash()).startsWith("$2a$");
        }

        @Test
        @DisplayName("un ADMIN ne peut pas creer un SUPER_ADMIN")
        void creation_adminNePeutPasCreerUnSuperAdmin() {
            when(repo.findByEmail(anyString())).thenReturn(Optional.empty());

            var req = new AdminCreateUserRequest("Pirate", "pirate@test.fr", null, "SUPER_ADMIN");

            assertThatThrownBy(() -> authService.createUserByAdmin(req, "ADMIN"))
                    .isInstanceOf(AccessDeniedException.class);

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("un SUPER_ADMIN peut creer un SUPER_ADMIN")
        void creation_superAdminPeutCreerUnSuperAdmin() {
            when(repo.findByEmail(anyString())).thenReturn(Optional.empty());
            when(mailDispatcher.send(anyString(), anyString(), anyString())).thenReturn(true);

            authService.createUserByAdmin(
                    new AdminCreateUserRequest("Boss", "boss@test.fr", null, "SUPER_ADMIN"), "SUPER_ADMIN");

            var capture = ArgumentCaptor.forClass(User.class);
            verify(repo).save(capture.capture());
            assertThat(capture.getValue().getRole()).isEqualTo(Role.SUPER_ADMIN);
        }

        @Test
        @DisplayName("un email deja utilise est refuse")
        void creation_refuseUnEmailDejaUtilise() {
            when(repo.findByEmail("pris@test.fr"))
                    .thenReturn(Optional.of(compte("pris@test.fr", "x", Role.USER)));

            assertThatThrownBy(() -> authService.createUserByAdmin(
                    new AdminCreateUserRequest("X", "pris@test.fr", null, "USER"), "ADMIN"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("le compte est cree meme si l'email ne part pas, et l'echec est remonte")
        void creation_survitAUnEchecEmail() {
            when(repo.findByEmail(anyString())).thenReturn(Optional.empty());
            when(mailDispatcher.send(anyString(), anyString(), anyString())).thenReturn(false);

            boolean envoye = authService.createUserByAdmin(
                    new AdminCreateUserRequest("N", "n@test.fr", null, "USER"), "ADMIN");

            assertThat(envoye)
                    .as("l'appelant doit pouvoir avertir que le mot de passe est perdu")
                    .isFalse();
            verify(repo).save(any());
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Connexion")
    class Connexion {

        @Test
        @DisplayName("un mot de passe temporaire expire verrouille le compte")
        void login_motDePasseExpireVerrouilleLeCompte() {
            var u = compte("expire@test.fr", "Temp123!", Role.USER);
            u.setPasswordExpiresAt(LocalDateTime.now().minusDays(1));
            when(repo.findByEmail("expire@test.fr")).thenReturn(Optional.of(u));

            assertThatThrownBy(() -> authService.login(new LoginRequest("expire@test.fr", "Temp123!")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("expir");

            assertThat(u.isAccountLocked())
                    .as("un mot de passe temporaire perime doit fermer le compte")
                    .isTrue();
            verify(repo).save(u);
        }

        @Test
        @DisplayName("un compte verrouille est refuse")
        void login_compteVerrouille() {
            var u = compte("bloque@test.fr", "Motdepasse1!", Role.USER);
            u.setAccountLocked(true);
            when(repo.findByEmail("bloque@test.fr")).thenReturn(Optional.of(u));

            assertThatThrownBy(() -> authService.login(new LoginRequest("bloque@test.fr", "Motdepasse1!")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("errouill");
        }

        @Test
        @DisplayName("un mot de passe temporaire encore valide laisse passer")
        void login_motDePasseTemporaireValide() {
            var u = compte("ok@test.fr", "Temp123!", Role.USER);
            u.setPasswordExpiresAt(LocalDateTime.now().plusDays(10));
            when(repo.findByEmail("ok@test.fr")).thenReturn(Optional.of(u));

            var res = authService.login(new LoginRequest("ok@test.fr", "Temp123!"));

            assertThat(res.accessToken()).isEqualTo("access-token");
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Changement de mot de passe par l'utilisateur")
    class ChangementMotDePasse {

        @Test
        @DisplayName("remplace le hash, sort du regime temporaire et horodate la revocation")
        void changement_remplaceLeHashEtSortDuRegimeTemporaire() {
            var u = compte("user@test.fr", "AncienMdp1!", Role.USER);
            u.setPasswordExpiresAt(LocalDateTime.now().plusDays(14));
            when(repo.findByEmailIgnoreCase("user@test.fr")).thenReturn(Optional.of(u));

            authService.changePassword("user@test.fr",
                    new ChangePasswordRequest("AncienMdp1!", "NouveauMdp1!"));

            assertThat(encoder.matches("NouveauMdp1!", u.getMotDePasseHash()))
                    .as("le nouveau mot de passe doit ouvrir le compte")
                    .isTrue();
            assertThat(encoder.matches("AncienMdp1!", u.getMotDePasseHash()))
                    .as("l'ancien mot de passe doit etre mort")
                    .isFalse();
            assertThat(u.getPasswordExpiresAt())
                    .as("le compte n'est plus temporaire, plus de verrouillage a 14 jours")
                    .isNull();
            assertThat(u.getPasswordChangedAt())
                    .as("sans cet horodatage les jetons deja emis survivraient")
                    .isNotNull();
            verify(repo).save(u);
        }

        @Test
        @DisplayName("un mot de passe actuel faux est refuse et ne modifie rien")
        void changement_refuseUnMotDePasseActuelFaux() {
            var u = compte("user@test.fr", "AncienMdp1!", Role.USER);
            String hashAvant = u.getMotDePasseHash();
            when(repo.findByEmailIgnoreCase("user@test.fr")).thenReturn(Optional.of(u));

            assertThatThrownBy(() -> authService.changePassword("user@test.fr",
                    new ChangePasswordRequest("PasLeBon!", "NouveauMdp1!")))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(u.getMotDePasseHash()).isEqualTo(hashAvant);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("reutiliser le mot de passe actuel est refuse")
        void changement_refuseLeMemeMotDePasse() {
            var u = compte("user@test.fr", "AncienMdp1!", Role.USER);
            when(repo.findByEmailIgnoreCase("user@test.fr")).thenReturn(Optional.of(u));

            assertThatThrownBy(() -> authService.changePassword("user@test.fr",
                    new ChangePasswordRequest("AncienMdp1!", "AncienMdp1!")))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("un compte inconnu leve une erreur")
        void changement_compteInconnu() {
            when(repo.findByEmailIgnoreCase("fantome@test.fr")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.changePassword("fantome@test.fr",
                    new ChangePasswordRequest("a", "NouveauMdp1!")))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Regeneration par un super admin")
    class Regeneration {

        @Test
        @DisplayName("remet un mot de passe temporaire, deverrouille et revoque les jetons")
        void regeneration_remetUnMotDePasseTemporaire() {
            var u = compte("bloque@test.fr", "Ancien1!", Role.USER);
            u.setAccountLocked(true);
            String hashAvant = u.getMotDePasseHash();
            when(repo.findById(7L)).thenReturn(Optional.of(u));
            when(mailDispatcher.send(anyString(), anyString(), anyString())).thenReturn(true);

            boolean envoye = authService.regeneratePassword(7L);

            assertThat(envoye).isTrue();
            assertThat(u.getMotDePasseHash()).isNotEqualTo(hashAvant);
            assertThat(u.isAccountLocked()).isFalse();
            assertThat(u.getPasswordExpiresAt()).isAfter(LocalDateTime.now().plusDays(13));
            assertThat(u.getPasswordChangedAt()).isNotNull();
        }

        @Test
        @DisplayName("un utilisateur inexistant leve une erreur")
        void regeneration_utilisateurInexistant() {
            when(repo.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.regeneratePassword(404L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
