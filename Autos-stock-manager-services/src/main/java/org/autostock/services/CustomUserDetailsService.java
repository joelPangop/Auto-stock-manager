package org.autostock.services;

import org.autostock.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        return User.withUsername(u.getEmail())
                .password(u.getMotDePasseHash())
                .roles(u.getRole().name())
                .build();
    }
}
