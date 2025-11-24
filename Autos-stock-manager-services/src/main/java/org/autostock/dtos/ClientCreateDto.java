package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClientCreateDto {
    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    // getters / setters
}
