package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserCreateDto {
    private String nom;
    private String email;
    private String motDePasse;
    private String role;
}
