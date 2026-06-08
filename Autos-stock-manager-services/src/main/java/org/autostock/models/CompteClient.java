package org.autostock.models;

import jakarta.persistence.*;
import lombok.*;
import org.autostock.enums.Role;

@Entity
@Table(name = "compte_client")
@Getter @Setter
@NoArgsConstructor
public class CompteClient extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String motDePasseHash;

    @Column(length = 20)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.CLIENT;
}
