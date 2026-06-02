package org.autostock.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autostock.enums.TypeFournisseur;

import java.util.*;

@Entity
@Table(name = "fournisseur")
@Getter @Setter
public class Fournisseur extends AbstractEntity {

    @Column(nullable = false, length = 150)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeFournisseur type; // ou Enum

    private String telephone;
    private String adresse;

    @OneToMany(mappedBy = "fournisseur")
    private List<Voiture> voitures = new ArrayList<>();

    @OneToMany(mappedBy = "fournisseur")
    private List<Depense> depenses = new ArrayList<>();

}
