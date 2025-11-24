package org.autostock.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    private final String allowedOrigins;

    public SecurityConfig(@Value("${app.cors.allowed-origins:*}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

//    @Bean
//    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // pratique pour API REST (sinon gère un token)
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/",                 // page d’accueil
//                                "/index.html",
//                                "/favicon.ico",
//                                "/error",
//                                "/assets/**",        // static (adapter selon ton projet)
//                                "/css/**", "/js/**", "/images/**",
//                                "/swagger-ui/**", "/v3/api-docs/**"
//                        ).permitAll()
//                        .requestMatchers("/api/**").permitAll() // ou .authenticated() si tu veux protéger l’API
//                        .anyRequest().authenticated()
//                )
//                .httpBasic(Customizer.withDefaults())   // optionnel (API)
//                .formLogin(form -> form.disable());     // désactive la page de login par défaut
//
//        return http.build();
//    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfiguration()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                );
        // H2 console
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private CorsConfigurationSource corsConfiguration() {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
