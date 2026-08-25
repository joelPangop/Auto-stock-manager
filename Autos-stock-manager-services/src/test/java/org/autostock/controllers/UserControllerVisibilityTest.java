package org.autostock.controllers;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.dtos.UserListDto;
import org.autostock.enums.Role;
import org.autostock.mappers.UserMapper;
import org.autostock.models.User;
import org.autostock.services.AuthService;
import org.autostock.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Visibilite des comptes SUPER_ADMIN.
 *
 * <p>La regle est verifiee au niveau du controleur et non de l'ecran Angular :
 * filtrer la liste cote navigateur ne protegerait rien, l'API renverrait
 * toujours les comptes et ils seraient lisibles dans l'onglet reseau.
 *
 * <p>Le refus attendu est « introuvable » et non « interdit ». Un 403
 * confirmerait l'existence du compte et permettrait a un ADMIN de cartographier
 * les SUPER_ADMIN en essayant les identifiants un par un.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserControllerVisibilityTest {

    @Mock private UserService userService;
    @Mock private UserMapper userMapper;
    @Mock private AuthService authService;

    private UserController controller;

    private User simpleUser;
    private User admin;
    private User superAdmin;

    @BeforeEach
    void setUp() {
        controller = new UserController();
        ReflectionTestUtils.setField(controller, "utilisateurService", userService);
        ReflectionTestUtils.setField(controller, "utilisateurMapper", userMapper);
        ReflectionTestUtils.setField(controller, "authService", authService);

        simpleUser = utilisateur(1L, "user@test.fr", Role.USER);
        admin = utilisateur(2L, "admin@test.fr", Role.ADMIN);
        superAdmin = utilisateur(3L, "boss@test.fr", Role.SUPER_ADMIN);

        when(userMapper.toListDto(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(inv -> {
                    User u = inv.getArgument(0);
                    var dto = new UserListDto();
                    dto.setId(u.getId());
                    dto.setEmail(u.getEmail());
                    dto.setRole(u.getRole().name());
                    return dto;
                });
    }

    private User utilisateur(Long id, String email, Role role) {
        var u = User.builder().nom("N").email(email).motDePasseHash("x").role(role).build();
        u.setId(id);
        return u;
    }

    /** Authentification portant une seule autorite, comme en production. */
    private Authentication authentifie(Role role) {
        return new UsernamePasswordAuthenticationToken(
                "principal", null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    // =====================================================================
    // Liste
    // =====================================================================

    @Test
    @DisplayName("un ADMIN ne voit aucun SUPER_ADMIN dans la liste")
    void liste_adminNeVoitPasLesSuperAdmin() {
        when(userService.findAll()).thenReturn(List.of(simpleUser, admin, superAdmin));

        List<UserListDto> vus = controller.list(null, authentifie(Role.ADMIN));

        assertThat(vus).extracting(UserListDto::getEmail)
                .containsExactlyInAnyOrder("user@test.fr", "admin@test.fr")
                .doesNotContain("boss@test.fr");
    }

    @Test
    @DisplayName("un SUPER_ADMIN voit tout le monde")
    void liste_superAdminVoitTout() {
        when(userService.findAll()).thenReturn(List.of(simpleUser, admin, superAdmin));

        List<UserListDto> vus = controller.list(null, authentifie(Role.SUPER_ADMIN));

        assertThat(vus).hasSize(3)
                .extracting(UserListDto::getEmail)
                .contains("boss@test.fr");
    }

    @Test
    @DisplayName("le filtre par role ne permet pas de contourner le masquage")
    void liste_filtreParRoleNeContournePasLeMasquage() {
        // Un ADMIN qui demande explicitement ?role=SUPER_ADMIN ne doit rien obtenir.
        when(userService.trouverParRole(Role.SUPER_ADMIN)).thenReturn(List.of(superAdmin));

        List<UserListDto> vus = controller.list("SUPER_ADMIN", authentifie(Role.ADMIN));

        assertThat(vus).isEmpty();
    }

    @Test
    @DisplayName("une authentification absente est traitee comme non privilegiee")
    void liste_authentificationNulleEstNonPrivilegiee() {
        when(userService.findAll()).thenReturn(List.of(simpleUser, superAdmin));

        List<UserListDto> vus = controller.list(null, null);

        assertThat(vus).extracting(UserListDto::getEmail).doesNotContain("boss@test.fr");
    }

    // =====================================================================
    // Acces unitaire
    // =====================================================================

    @Test
    @DisplayName("un ADMIN qui cible un SUPER_ADMIN par identifiant obtient « introuvable »")
    void get_adminNePeutPasLireUnSuperAdmin() {
        when(userService.findById(3L)).thenReturn(Optional.of(superAdmin));

        assertThatThrownBy(() -> controller.get(3L, authentifie(Role.ADMIN)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("un SUPER_ADMIN peut lire un SUPER_ADMIN")
    void get_superAdminPeutLireUnSuperAdmin() {
        when(userService.findById(3L)).thenReturn(Optional.of(superAdmin));

        controller.get(3L, authentifie(Role.SUPER_ADMIN));

        verify(userMapper).toDto(superAdmin);
    }

    @Test
    @DisplayName("un ADMIN ne peut pas supprimer un SUPER_ADMIN")
    void delete_adminNePeutPasSupprimerUnSuperAdmin() {
        when(userService.findById(3L)).thenReturn(Optional.of(superAdmin));

        assertThatThrownBy(() -> controller.delete(3L, authentifie(Role.ADMIN)))
                .isInstanceOf(EntityNotFoundException.class);

        verify(userService, never()).deleteById(3L);
    }

    @Test
    @DisplayName("un ADMIN peut supprimer un utilisateur ordinaire")
    void delete_adminPeutSupprimerUnUtilisateurOrdinaire() {
        when(userService.findById(1L)).thenReturn(Optional.of(simpleUser));

        controller.delete(1L, authentifie(Role.ADMIN));

        verify(userService).deleteById(1L);
    }

    @Test
    @DisplayName("un ADMIN ne peut pas modifier un SUPER_ADMIN")
    void update_adminNePeutPasModifierUnSuperAdmin() throws Exception {
        when(userService.findById(3L)).thenReturn(Optional.of(superAdmin));

        assertThatThrownBy(() -> controller.update(3L, new org.autostock.dtos.UserUpdateDto(),
                authentifie(Role.ADMIN)))
                .isInstanceOf(EntityNotFoundException.class);

        verify(userService, never()).create(org.mockito.ArgumentMatchers.any());
    }

    // =====================================================================
    // Role du createur
    // =====================================================================

    @Test
    @DisplayName("le role du createur est deduit de la presence de ROLE_SUPER_ADMIN, pas du premier element")
    void adminCreate_deduitLeRoleDuCreateur() {
        // Deux autorites, SUPER_ADMIN en second : une lecture par findFirst()
        // conclurait a tort que le createur est un simple ADMIN.
        var auth = new UsernamePasswordAuthenticationToken("principal", null, List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));

        when(authService.createUserByAdmin(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq("SUPER_ADMIN"))).thenReturn(true);

        var req = new org.autostock.dtos.auth.AdminCreateUserRequest(
                "Nouveau", "n@test.fr", null, "SUPER_ADMIN");

        var res = controller.adminCreate(req, auth);

        assertThat(res.getBody()).containsEntry("emailSent", true);
        verify(authService).createUserByAdmin(req, "SUPER_ADMIN");
    }

    @Test
    @DisplayName("adminCreate remonte l'echec d'envoi au lieu d'annoncer un succes")
    void adminCreate_remonteEchecEmail() {
        when(authService.createUserByAdmin(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString())).thenReturn(false);

        var res = controller.adminCreate(
                new org.autostock.dtos.auth.AdminCreateUserRequest("N", "n@test.fr", null, "USER"),
                authentifie(Role.ADMIN));

        assertThat(res.getBody()).containsEntry("emailSent", false);
    }
}
