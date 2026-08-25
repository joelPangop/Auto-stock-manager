package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.enums.MethodePaiement;
import org.autostock.mappers.PaiementMapper;
import org.autostock.models.Paiement;
import org.autostock.models.Vente;
import org.autostock.repositories.PaiementRepository;
import org.autostock.repositories.UserRepository;
import org.autostock.repositories.VenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires purs (aucun contexte Spring, aucune base) de PaiementServiceImpl.
 *
 * Règles couvertes :
 *  - ajouterPaiement() rejette une méthode de paiement inconnue (avant toute sauvegarde).
 *  - totalPaye() est la somme des montants des paiements de la vente.
 *  - resteAPayer() = prixFinal - totalPaye(), sans aucune borne (peut devenir négatif :
 *    le code ne bloque pas les sur-paiements — comportement documenté ici tel quel).
 */
@ExtendWith(MockitoExtension.class)
class PaiementServiceImplTest {

    @Mock
    private VenteRepository venteRepository;
    @Mock
    private PaiementMapper mapper;
    @Mock
    private PaiementRepository repository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaiementServiceImpl paiementService;

    private Vente vente;

    @BeforeEach
    void setUp() {
        // Le champ 'repository' est declare dans AbstractBaseService avec un type
        // generique <R extends JpaRepository>. Apres effacement, @InjectMocks voit
        // plusieurs mocks assignables a JpaRepository et n'alimente pas le champ :
        // il reste null. On l'injecte donc explicitement.
        ReflectionTestUtils.setField(paiementService, "repository", repository);
        vente = new Vente();
        vente.setId(5L);
        vente.setPrixFinal(BigDecimal.valueOf(24000));
    }

    @Test
    @DisplayName("ajouterPaiement() enregistre un paiement avec la méthode fournie")
    void ajouterPaiement_enregistreLePaiement() {
        when(venteRepository.findById(5L)).thenReturn(Optional.of(vente));
        when(repository.save(any(Paiement.class))).thenAnswer(inv -> inv.getArgument(0));

        Paiement resultat = paiementService.ajouterPaiement(5L, BigDecimal.valueOf(1000), "CARD");

        assertThat(resultat.getVente()).isEqualTo(vente);
        assertThat(resultat.getMontant()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(resultat.getMethode()).isEqualTo(MethodePaiement.CARTE);
        assertThat(resultat.getDatePaiement()).isNotNull();
    }

    @Test
    @DisplayName("ajouterPaiement() rejette une méthode de paiement inconnue et ne sauvegarde rien")
    void ajouterPaiement_methodeInconnue() {
        when(venteRepository.findById(5L)).thenReturn(Optional.of(vente));

        assertThatThrownBy(() -> paiementService.ajouterPaiement(5L, BigDecimal.valueOf(1000), "BITCOIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Methode de paiement inconnue");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("ajouterPaiement() sur une vente inexistante lève EntityNotFoundException")
    void ajouterPaiement_venteInexistante() {
        when(venteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paiementService.ajouterPaiement(999L, BigDecimal.valueOf(1000), "CASH"))
                .isInstanceOf(EntityNotFoundException.class);

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("totalPaye() additionne les montants de tous les paiements de la vente")
    void totalPaye_additionneLesMontants() {
        Paiement p1 = new Paiement();
        p1.setMontant(BigDecimal.valueOf(1000));
        Paiement p2 = new Paiement();
        p2.setMontant(BigDecimal.valueOf(2500));
        when(repository.findByVente_Id(5L)).thenReturn(List.of(p1, p2));

        assertThat(paiementService.totalPaye(5L)).isEqualByComparingTo(BigDecimal.valueOf(3500));
    }

    @Test
    @DisplayName("totalPaye() renvoie zéro quand aucun paiement n'a été enregistré")
    void totalPaye_zeroSansPaiement() {
        when(repository.findByVente_Id(5L)).thenReturn(List.of());

        assertThat(paiementService.totalPaye(5L)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("resteAPayer() soustrait le total payé du prix final")
    void resteAPayer_soustraitLeTotalPaye() {
        Paiement p1 = new Paiement();
        p1.setMontant(BigDecimal.valueOf(10000));
        when(venteRepository.findById(5L)).thenReturn(Optional.of(vente));
        when(repository.findByVente_Id(5L)).thenReturn(List.of(p1));

        assertThat(paiementService.resteAPayer(5L)).isEqualByComparingTo(BigDecimal.valueOf(14000));
    }

    @Test
    @DisplayName("resteAPayer() peut devenir négatif : aucun contrôle de sur-paiement dans le code actuel")
    void resteAPayer_peutDevenirNegatif() {
        Paiement p1 = new Paiement();
        p1.setMontant(BigDecimal.valueOf(30000));
        when(venteRepository.findById(5L)).thenReturn(Optional.of(vente));
        when(repository.findByVente_Id(5L)).thenReturn(List.of(p1));

        assertThat(paiementService.resteAPayer(5L)).isEqualByComparingTo(BigDecimal.valueOf(-6000));
    }

    @Test
    @DisplayName("resteAPayer() sur une vente inexistante lève EntityNotFoundException")
    void resteAPayer_venteInexistante() {
        when(venteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paiementService.resteAPayer(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
