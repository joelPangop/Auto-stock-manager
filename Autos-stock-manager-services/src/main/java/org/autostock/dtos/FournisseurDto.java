package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;
import org.autostock.enums.TypeFournisseur;

@Getter
@Setter
public class FournisseurDto {
    private Long id;
    private String nom;
    private TypeFournisseur type;
    private String telephone;
    private String adresse;
}
