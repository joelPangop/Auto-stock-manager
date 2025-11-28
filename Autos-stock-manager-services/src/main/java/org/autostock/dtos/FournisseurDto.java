package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FournisseurDto {
    private Long id;
    private String nom;
    private String type;
    private String telephone;
    private String adresse;
}
