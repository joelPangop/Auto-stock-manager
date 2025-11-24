package org.autostock.dtos.auth;


public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long   expiresIn,
        UserView user
) {
    public static record UserView(Long id, String nom, String email, String role) {}
}
