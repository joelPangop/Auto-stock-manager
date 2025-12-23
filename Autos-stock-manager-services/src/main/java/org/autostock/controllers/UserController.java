package org.autostock.controllers;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.autostock.dtos.UserCreateDto;
import org.autostock.dtos.UserDto;
import org.autostock.dtos.UserListDto;
import org.autostock.dtos.UserUpdateDto;
import org.autostock.enums.Role;
import org.autostock.mappers.UserMapper;
import org.autostock.models.User;
import org.autostock.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService utilisateurService;

    @Autowired
    private UserMapper utilisateurMapper;
    // Optionnel : encoder si Spring Security présent
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init(){
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto create(@RequestBody UserCreateDto dto) throws AccessDeniedException {
        String hash = passwordEncoder != null ? passwordEncoder.encode(dto.getMotDePasse()) : dto.getMotDePasse();
        User saved = utilisateurService.create(utilisateurMapper.toEntity(dto, hash));
        return utilisateurMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto update(@PathVariable Long id, @RequestBody UserUpdateDto dto) throws AccessDeniedException {
        User u = utilisateurService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
        utilisateurMapper.updateEntity(u, dto);
        return utilisateurMapper.toDto(utilisateurService.create(u));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto get(@PathVariable Long id) {
        return utilisateurService.findById(id)
                .map(utilisateurMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserListDto> list(@RequestParam(required = false) String role) {
        var list = (role == null || role.isBlank())
                ? utilisateurService.findAll()
                : utilisateurService.trouverParRole(Role.valueOf(role.toUpperCase()));
        return list.stream().map(utilisateurMapper::toListDto).toList();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        utilisateurService.deleteById(id);
    }
}
