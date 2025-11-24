package org.autostock.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "client")
@Getter
@Setter
public class Client extends AbstractEntity {

    private String nom;
    private String email;
    private String telephone;
    private String adresse;

    @OneToMany(mappedBy = "client")
    private List<Vente> ventes = new ArrayList<>();
}
