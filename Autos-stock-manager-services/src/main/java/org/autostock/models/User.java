package org.autostock.models;

import jakarta.persistence.*;
import lombok.*;
import org.autostock.enums.Role;

import java.util.*;

@Entity
@Table(name = "utilisateur")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractEntity {
    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String motDePasseHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "vendeur")
    private List<Vente> ventes = new ArrayList<>();

    public User(Long id){
       setId(id);
    }
}
