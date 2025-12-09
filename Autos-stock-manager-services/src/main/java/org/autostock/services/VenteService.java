package org.autostock.services;

import org.autostock.models.User;
import org.autostock.models.Vente;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VenteService extends IService<Vente, Long> {

    Vente creerVente(Long idVoiture, Long idClient, Long idVendeur,
                     BigDecimal prixFinal, String modePaiement) throws AccessDeniedException;

    List<Vente> ventesDuClient(Long idClient);

    List<Vente> ventesDuVendeur(Long idVendeur);

    List<Vente> ventesEntre(LocalDateTime debut, LocalDateTime fin);

    Optional<Vente> findById(Long idVente);
}
