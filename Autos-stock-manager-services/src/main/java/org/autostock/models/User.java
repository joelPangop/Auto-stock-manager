package org.autostock.models;

import jakarta.persistence.*;
import lombok.*;
import org.autostock.enums.Role;

import java.time.LocalDateTime;
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

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;

    @OneToMany(mappedBy = "vendeur")
    private List<Vente> ventes = new ArrayList<>();

    public User(Long id){
       setId(id);
    }
}
