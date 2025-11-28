package org.autostock.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autostock.enums.StatutVoiture;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "voiture")
@Getter @Setter
public class Voiture extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_modele")
    private Modele modele;

    @ManyToOne
    @JoinColumn(name = "id_fournisseur")
    private Fournisseur fournisseur;

    private Integer annee;

    private String couleur;

    @Column(nullable = false, unique = true, length = 50)
    private String vin;

    private BigDecimal prixAchat;
    private BigDecimal prixVente;

    @Column(nullable = false)
    private LocalDateTime dateEntreeStock;

    private Long kilometrage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutVoiture statut = StatutVoiture.EN_STOCK;

    @OneToMany(mappedBy = "voiture")
    private List<StockMouvement> mouvements = new ArrayList<>();

    @OneToOne(mappedBy = "voiture")
    private Vente vente;

}
