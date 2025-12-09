package org.autostock.configs;

import org.autostock.dtos.UserDto;
import org.autostock.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

// SecurityUtils.java
@Component
public class SecurityUtils {

    @Autowired
    UserRepository userRepository;

    public Long currentUserId() throws AccessDeniedException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails ud)) {
            throw new AccessDeniedException("Non authentifié");
        }
        // adapte si tu mets l’id dans les authorities/claims :
        return (userRepository.findByEmail(ud.getUsername())).get().getId(); // ou extraire depuis le JWT
    }

    public boolean isAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
