package org.autostock.services;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.client.*;
import org.autostock.models.CompteClient;
import org.autostock.repositories.CompteClientRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CompteClientServiceImpl implements CompteClientService {

    private final CompteClientRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public ClientAuthResponse register(CompteClientRegisterDto dto) {
        if (repo.existsByEmailIgnoreCase(dto.email())) {
            throw new IllegalStateException("Un compte existe déjà avec cet email.");
        }
        var client = new CompteClient();
        client.setNom(dto.nom());
        client.setEmail(dto.email().toLowerCase());
        client.setMotDePasseHash(passwordEncoder.encode(dto.motDePasse()));
        client.setTelephone(dto.telephone());
        repo.save(client);

        String token = jwtService.generateTokenForClient(client);
        return new ClientAuthResponse(token, CompteClientDto.from(client));
    }

    @Override
    public ClientAuthResponse login(CompteClientLoginDto dto) {
        var client = repo.findByEmailIgnoreCase(dto.email())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect."));
        if (!passwordEncoder.matches(dto.motDePasse(), client.getMotDePasseHash())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect.");
        }
        String token = jwtService.generateTokenForClient(client);
        return new ClientAuthResponse(token, CompteClientDto.from(client));
    }

    @Override
    @Transactional(readOnly = true)
    public CompteClientDto getProfile(String email) {
        var client = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Compte introuvable."));
        return CompteClientDto.from(client);
    }

    @Override
    public CompteClientDto updateProfile(String email, CompteClientUpdateDto dto) {
        var client = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Compte introuvable."));
        if (dto.nom() != null && !dto.nom().isBlank()) client.setNom(dto.nom());
        if (dto.telephone() != null) client.setTelephone(dto.telephone());
        return CompteClientDto.from(client);
    }
}
