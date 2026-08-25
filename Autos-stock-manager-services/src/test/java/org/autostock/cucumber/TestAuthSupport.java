package org.autostock.cucumber;

import org.autostock.models.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Simule l'authentification pour les tests de service : SecurityUtils
 * (org.autostock.configs.SecurityUtils) lit l'utilisateur courant dans le
 * SecurityContextHolder puis le retrouve en base par email. On construit
 * donc un principal Spring Security minimal dont le username == email de
 * l'utilisateur de test, sans passer par le filtre JWT réel.
 */
@Component
public class TestAuthSupport {

    public void connecterEnTantQue(User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        var principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("N/A")
                .authorities(authorities)
                .build();
        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public void deconnecter() {
        SecurityContextHolder.clearContext();
    }
}
