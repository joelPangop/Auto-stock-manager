package org.autostock.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modele")
@Getter @Setter
public class Modele extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_marque")
    private Marque marque;

    @Column(nullable = false, length = 100)
    private String nom;

    @OneToMany(mappedBy = "modele")
    private List<Voiture> voitures = new ArrayList<>();
}
