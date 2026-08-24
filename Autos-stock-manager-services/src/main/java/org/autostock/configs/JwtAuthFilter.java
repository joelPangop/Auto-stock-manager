package org.autostock.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.autostock.repositories.UserRepository;
import org.autostock.services.CustomUserDetailsService;
import org.autostock.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwt;

    @Autowired
    private CustomUserDetailsService uds;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwt.isTokenValid(token)) {
                try {
                    String username = jwt.extractUsername(token);

                    if (issuedBeforePasswordChange(username, token)) {
                        // Jeton anterieur au dernier changement de mot de passe :
                        // la session continuerait sinon a fonctionner avec des
                        // identifiants revoques. On laisse passer sans
                        // authentifier, la requete finira en 401.
                        chain.doFilter(req, res);
                        return;
                    }

                    var userDetails = uds.loadUserByUsername(username);
                    var auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception ignored) {
                    // token valide mais utilisateur supprimé → continuer sans authentification
                }
            }
        }
        chain.doFilter(req, res);
    }

    /**
     * Vrai si le jeton a ete emis avant le dernier changement de mot de passe.
     * Ne concerne que le personnel interne ; les comptes clients du portail ne
     * portent pas cette date et ne sont donc pas filtres ici.
     */
    private boolean issuedBeforePasswordChange(String email, String token) {
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getPasswordChangedAt() == null) return false;

        var issuedAt = jwt.extractIssuedAt(token);
        if (issuedAt == null) return false;

        // iat est arrondi a la seconde : la date de changement l'est aussi a
        // l'ecriture, sinon un jeton emis dans la meme seconde serait rejete.
        LocalDateTime issued = LocalDateTime.ofInstant(issuedAt.toInstant(), ZoneId.systemDefault());
        return issued.isBefore(user.getPasswordChangedAt());
    }
}
