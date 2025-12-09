package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ClientDto {
    private Long id;
    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    private LocalDateTime createAt;
    // getters / setters
}
