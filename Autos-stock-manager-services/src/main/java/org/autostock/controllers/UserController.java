package org.autostock.controllers;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.autostock.dtos.UserCreateDto;
import org.autostock.dtos.UserDto;
import org.autostock.dtos.UserListDto;
import org.autostock.dtos.UserUpdateDto;
import org.autostock.dtos.auth.AdminCreateUserRequest;
import org.autostock.enums.Role;
import org.autostock.mappers.UserMapper;
import org.autostock.models.User;
import org.autostock.services.AuthService;
import org.autostock.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService utilisateurService;

    @Autowired
    private UserMapper utilisateurMapper;

    @Autowired
    private AuthService authService;
    // Optionnel : encoder si Spring Security présent
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init(){
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // -------------------------------------------------------------------------
    // Visibilité des comptes SUPER_ADMIN
    //
    // Un ADMIN ne doit ni lister, ni consulter, ni modifier un SUPER_ADMIN.
    // La règle est appliquée ici et pas seulement dans l'interface : sans ça,
    // un simple appel a l'API suffirait a contourner le filtrage de l'écran.
    //
    // Les accès refusés renvoient « introuvable » plutôt qu'« interdit » : un
    // ADMIN qui ne doit pas voir ces comptes ne doit pas non plus pouvoir
    // déduire leur existence en essayant des identifiants.
    // -------------------------------------------------------------------------

    private boolean isSuperAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }

    /** Charge un utilisateur en masquant les SUPER_ADMIN aux simples ADMIN. */
    private User loadVisible(Long id, Authentication auth) {
        User u = utilisateurService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
        if (u.getRole() == Role.SUPER_ADMIN && !isSuperAdmin(auth)) {
            throw new EntityNotFoundException("Utilisateur introuvable");
        }
        return u;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public UserDto create(@RequestBody UserCreateDto dto) throws AccessDeniedException {
        String hash = passwordEncoder != null ? passwordEncoder.encode(dto.getMotDePasse()) : dto.getMotDePasse();
        User saved = utilisateurService.create(utilisateurMapper.toEntity(dto, hash));
        return utilisateurMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public UserDto update(@PathVariable Long id, @RequestBody UserUpdateDto dto, Authentication auth) throws AccessDeniedException {
        User u = loadVisible(id, auth);
        utilisateurMapper.updateEntity(u, dto);
        return utilisateurMapper.toDto(utilisateurService.create(u));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public UserDto get(@PathVariable Long id, Authentication auth) {
        return utilisateurMapper.toDto(loadVisible(id, auth));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<UserListDto> list(@RequestParam(required = false) String role, Authentication auth) {
        var list = (role == null || role.isBlank())
                ? utilisateurService.findAll()
                : utilisateurService.trouverParRole(Role.valueOf(role.toUpperCase()));

        boolean superAdmin = isSuperAdmin(auth);
        return list.stream()
                .filter(u -> superAdmin || u.getRole() != Role.SUPER_ADMIN)
                .map(utilisateurMapper::toListDto)
                .toList();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        loadVisible(id, auth);
        utilisateurService.deleteById(id);
    }

    @PostMapping("/admin-create")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Boolean>> adminCreate(@Valid @RequestBody AdminCreateUserRequest req, Authentication auth) {
        String creatorRole = isSuperAdmin(auth) ? "SUPER_ADMIN" : "ADMIN";
        boolean emailSent = authService.createUserByAdmin(req, creatorRole);
        // Le compte est créé quoi qu'il arrive ; emailSent dit à l'admin si
        // l'invitation est réellement partie ou si le mot de passe est perdu.
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("emailSent", emailSent));
    }

    @PostMapping("/{id}/regenerate-password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Boolean>> regeneratePassword(@PathVariable Long id) {
        boolean emailSent = authService.regeneratePassword(id);
        return ResponseEntity.ok(Map.of("emailSent", emailSent));
    }
}
