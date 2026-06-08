package org.autostock.services;

import org.autostock.repositories.CompteClientRepository;
import org.autostock.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Charge les UserDetails depuis:
 * 1. UserRepository   (personnel interne : ADMIN, VENDEUR…)
 * 2. CompteClientRepository (clients du portail Ted Auto : CLIENT)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompteClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Cherche d'abord dans les utilisateurs internes
        var internalUser = userRepository.findByEmail(email);
        if (internalUser.isPresent()) {
            var u = internalUser.get();
            return User.withUsername(u.getEmail())
                    .password(u.getMotDePasseHash())
                    .roles(u.getRole().name())
                    .build();
        }

        // Cherche ensuite dans les comptes client du portail
        return clientRepository.findByEmailIgnoreCase(email)
                .map(c -> User.withUsername(c.getEmail())
                        .password(c.getMotDePasseHash())
                        .roles(c.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
    }
}
