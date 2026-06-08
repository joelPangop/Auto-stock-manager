package org.autostock.dtos.client;

import org.autostock.models.CompteClient;

public record CompteClientDto(
        Long id,
        String nom,
        String email,
        String telephone
) {
    public static CompteClientDto from(CompteClient c) {
        return new CompteClientDto(c.getId(), c.getNom(), c.getEmail(), c.getTelephone());
    }
}
