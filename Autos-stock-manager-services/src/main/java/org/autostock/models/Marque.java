package org.autostock.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "marque")
@Getter @Setter
public class Marque extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String nom;

    @OneToMany(mappedBy = "marque")
    private List<Modele> modeles = new ArrayList<>();
}
