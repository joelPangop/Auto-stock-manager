package org.autostock.dtos.client;

public record ClientAuthResponse(
        String token,
        CompteClientDto client
) {}
