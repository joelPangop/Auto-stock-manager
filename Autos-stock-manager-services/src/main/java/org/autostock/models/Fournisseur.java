package org.autostock.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "fournisseur")
@Getter @Setter
public class Fournisseur extends AbstractEntity {

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 20)
    private String type; // ou Enum

    private String telephone;
    private String adresse;

    @OneToMany(mappedBy = "fournisseur")
    private List<Voiture> voitures = new ArrayList<>();
}
