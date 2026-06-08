package org.autostock.services;

import lombok.extern.slf4j.Slf4j;
import org.autostock.dtos.client.ReservationCreateDto;
import org.autostock.dtos.client.ReservationDto;
import org.autostock.enums.StatutReservation;
import org.autostock.models.Reservation;
import org.autostock.repositories.CompteClientRepository;
import org.autostock.repositories.ReservationRepository;
import org.autostock.repositories.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepo;
    private final CompteClientRepository clientRepo;
    private final VoitureRepository voitureRepo;

    @Autowired(required = false)
    private SesEmailService sesEmailService;

    public ReservationServiceImpl(ReservationRepository reservationRepo,
                                  CompteClientRepository clientRepo,
                                  VoitureRepository voitureRepo) {
        this.reservationRepo = reservationRepo;
        this.clientRepo = clientRepo;
        this.voitureRepo = voitureRepo;
    }

    @Override
    public ReservationDto create(String clientEmail, ReservationCreateDto dto) {
        var client = clientRepo.findByEmailIgnoreCase(clientEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client introuvable"));
        var voiture = voitureRepo.findById(dto.voitureId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable"));

        // Vérifier qu'il n'y a pas déjà une réservation active
        boolean existe = reservationRepo.existsByCompteClient_IdAndVoiture_IdAndStatutNot(
                client.getId(), voiture.getId(), StatutReservation.ANNULEE);
        if (existe) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Vous avez déjà une réservation active pour ce véhicule.");
        }

        var res = new Reservation();
        res.setCompteClient(client);
        res.setVoiture(voiture);
        res.setDateVisite(dto.dateVisite());
        res.setMessage(dto.message());
        reservationRepo.save(res);

        // Notification email (meilleur effort — SES peut être absent en test)
        if (sesEmailService != null) {
            try {
                String label = voiture.getModele().getMarque().getNom()
                        + " " + voiture.getModele().getNom() + " (" + voiture.getAnnee() + ")";
                sesEmailService.sendReservationConfirmation(client.getEmail(), client.getNom(), label, dto.dateVisite());
            } catch (Exception e) {
                log.warn("[Reservation] Envoi email confirmation échoué : {}", e.getMessage());
            }
        }

        log.info("[Reservation] Nouvelle réservation #{}  client={} voiture={}", res.getId(), clientEmail, voiture.getId());
        return ReservationDto.from(res);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDto> listByClient(String clientEmail) {
        var client = clientRepo.findByEmailIgnoreCase(clientEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client introuvable"));
        return reservationRepo.findByCompteClient_IdOrderByCreatedAtDesc(client.getId())
                .stream().map(ReservationDto::from).toList();
    }

    @Override
    public void cancel(String clientEmail, Long reservationId) {
        var res = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation introuvable"));
        if (!res.getCompteClient().getEmail().equalsIgnoreCase(clientEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        if (res.getStatut() == StatutReservation.ANNULEE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Réservation déjà annulée");
        }
        res.setStatut(StatutReservation.ANNULEE);
        log.info("[Reservation] Annulation réservation #{}", reservationId);
    }
}
