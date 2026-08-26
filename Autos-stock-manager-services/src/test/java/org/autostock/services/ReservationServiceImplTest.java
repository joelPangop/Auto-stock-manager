package org.autostock.services;

import org.autostock.dtos.client.ReservationCreateDto;
import org.autostock.enums.StatutReservation;
import org.autostock.models.CompteClient;
import org.autostock.models.Marque;
import org.autostock.models.Modele;
import org.autostock.models.Reservation;
import org.autostock.models.Voiture;
import org.autostock.repositories.CompteClientRepository;
import org.autostock.repositories.ReservationRepository;
import org.autostock.repositories.VoitureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Reservations du portail client Ted Auto.
 *
 * <p>Deux exigences dominent ici. D'abord le cloisonnement : un client ne doit
 * pouvoir agir que sur ses propres reservations, et l'identite vient du jeton,
 * jamais du corps de la requete. Ensuite la resilience de l'email : la
 * confirmation part en meilleur effort, une panne du canal ne doit pas annuler
 * une reservation valide.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservationServiceImplTest {

    @Mock private ReservationRepository reservationRepo;
    @Mock private CompteClientRepository clientRepo;
    @Mock private VoitureRepository voitureRepo;
    @Mock private MailDispatcher mailDispatcher;

    private ReservationServiceImpl service;

    private CompteClient client;
    private Voiture voiture;

    @BeforeEach
    void setUp() {
        service = new ReservationServiceImpl(reservationRepo, clientRepo, voitureRepo);
        ReflectionTestUtils.setField(service, "mailDispatcher", mailDispatcher);

        client = new CompteClient();
        client.setId(1L);
        client.setNom("Marc Client");
        client.setEmail("marc@test.fr");

        var marque = new Marque();
        marque.setNom("Honda");
        var modele = new Modele();
        modele.setNom("Civic");
        modele.setMarque(marque);

        voiture = new Voiture();
        voiture.setId(10L);
        voiture.setAnnee(2022);
        voiture.setModele(modele);

        when(clientRepo.findByEmailIgnoreCase("marc@test.fr")).thenReturn(Optional.of(client));
        when(voitureRepo.findById(10L)).thenReturn(Optional.of(voiture));
        when(mailDispatcher.send(anyString(), anyString(), anyString())).thenReturn(true);
        when(reservationRepo.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private ReservationCreateDto demande() {
        return new ReservationCreateDto(10L, LocalDate.of(2026, 9, 15), "Disponible le matin");
    }

    // =====================================================================
    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("la reservation est rattachee au client du jeton et a la voiture demandee")
        void creation_rattacheClientEtVoiture() {
            var capture = org.mockito.ArgumentCaptor.forClass(Reservation.class);

            service.create("marc@test.fr", demande());

            verify(reservationRepo).save(capture.capture());
            assertThat(capture.getValue().getCompteClient()).isSameAs(client);
            assertThat(capture.getValue().getVoiture()).isSameAs(voiture);
        }

        @Test
        @DisplayName("une reservation neuve est en attente")
        void creation_statutInitial() {
            var capture = org.mockito.ArgumentCaptor.forClass(Reservation.class);

            service.create("marc@test.fr", demande());

            verify(reservationRepo).save(capture.capture());
            assertThat(capture.getValue().getStatut()).isEqualTo(StatutReservation.EN_ATTENTE);
        }

        @Test
        @DisplayName("un email inconnu est refuse en 401, pas en 404")
        void creation_clientInconnu() {
            when(clientRepo.findByEmailIgnoreCase("inconnu@test.fr")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create("inconnu@test.fr", demande()))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("un vehicule inexistant renvoie 404")
        void creation_voitureInexistante() {
            when(voitureRepo.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create("marc@test.fr",
                    new ReservationCreateDto(404L, null, null)))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("une reservation active sur le meme vehicule est refusee en 409")
        void creation_doublonRefuse() {
            when(reservationRepo.existsByCompteClient_IdAndVoiture_IdAndStatutNot(
                    1L, 10L, StatutReservation.ANNULEE)).thenReturn(true);

            assertThatThrownBy(() -> service.create("marc@test.fr", demande()))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.CONFLICT);

            verify(reservationRepo, never()).save(any());
        }

        @Test
        @DisplayName("une reservation annulee ne bloque pas une nouvelle demande")
        void creation_apresAnnulationAutorisee() {
            when(reservationRepo.existsByCompteClient_IdAndVoiture_IdAndStatutNot(
                    1L, 10L, StatutReservation.ANNULEE)).thenReturn(false);

            service.create("marc@test.fr", demande());

            verify(reservationRepo).save(any());
        }

        @Test
        @DisplayName("la confirmation part par email au client")
        void creation_envoieLaConfirmation() {
            service.create("marc@test.fr", demande());

            verify(mailDispatcher).send(
                    org.mockito.ArgumentMatchers.eq("marc@test.fr"),
                    anyString(),
                    org.mockito.ArgumentMatchers.contains("Civic"));
        }

        @Test
        @DisplayName("un email en echec n annule pas la reservation")
        void creation_survitAUnEchecEmail() {
            when(mailDispatcher.send(anyString(), anyString(), anyString())).thenReturn(false);

            var dto = service.create("marc@test.fr", demande());

            assertThat(dto)
                    .as("la reservation est un acte metier, l email n est qu une notification")
                    .isNotNull();
            verify(reservationRepo).save(any());
        }

        @Test
        @DisplayName("une date de visite absente reste acceptee")
        void creation_sansDateDeVisite() {
            var capture = org.mockito.ArgumentCaptor.forClass(Reservation.class);

            service.create("marc@test.fr", new ReservationCreateDto(10L, null, null));

            verify(reservationRepo).save(capture.capture());
            assertThat(capture.getValue().getDateVisite()).isNull();
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Consultation")
    class Consultation {

        @Test
        @DisplayName("un client ne lit que ses propres reservations")
        void liste_filtreParClient() {
            var r = new Reservation();
            r.setCompteClient(client);
            r.setVoiture(voiture);
            when(reservationRepo.findByCompteClient_IdOrderByCreatedAtDesc(1L)).thenReturn(List.of(r));

            var liste = service.listByClient("marc@test.fr");

            assertThat(liste).hasSize(1);
            verify(reservationRepo).findByCompteClient_IdOrderByCreatedAtDesc(1L);
        }

        @Test
        @DisplayName("un email inconnu ne peut rien lire")
        void liste_clientInconnu() {
            when(clientRepo.findByEmailIgnoreCase("inconnu@test.fr")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.listByClient("inconnu@test.fr"))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Annulation")
    class Annulation {

        private Reservation reservationDe(CompteClient proprietaire, StatutReservation statut) {
            var r = new Reservation();
            r.setId(50L);
            r.setCompteClient(proprietaire);
            r.setVoiture(voiture);
            r.setStatut(statut);
            return r;
        }

        @Test
        @DisplayName("le proprietaire peut annuler sa reservation")
        void annulation_parLeProprietaire() {
            var r = reservationDe(client, StatutReservation.EN_ATTENTE);
            when(reservationRepo.findById(50L)).thenReturn(Optional.of(r));

            service.cancel("marc@test.fr", 50L);

            assertThat(r.getStatut()).isEqualTo(StatutReservation.ANNULEE);
        }

        @Test
        @DisplayName("l email est compare sans tenir compte de la casse")
        void annulation_casseIgnoree() {
            var r = reservationDe(client, StatutReservation.EN_ATTENTE);
            when(reservationRepo.findById(50L)).thenReturn(Optional.of(r));

            service.cancel("MARC@Test.FR", 50L);

            assertThat(r.getStatut()).isEqualTo(StatutReservation.ANNULEE);
        }

        @Test
        @DisplayName("un autre client ne peut pas annuler : 403 et statut inchange")
        void annulation_parUnAutreClientRefusee() {
            var autre = new CompteClient();
            autre.setId(2L);
            autre.setEmail("intrus@test.fr");

            var r = reservationDe(autre, StatutReservation.EN_ATTENTE);
            when(reservationRepo.findById(50L)).thenReturn(Optional.of(r));

            assertThatThrownBy(() -> service.cancel("marc@test.fr", 50L))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);

            assertThat(r.getStatut())
                    .as("la reservation d autrui doit rester intacte")
                    .isEqualTo(StatutReservation.EN_ATTENTE);
        }

        @Test
        @DisplayName("annuler deux fois est refuse en 400")
        void annulation_dejaAnnulee() {
            var r = reservationDe(client, StatutReservation.ANNULEE);
            when(reservationRepo.findById(50L)).thenReturn(Optional.of(r));

            assertThatThrownBy(() -> service.cancel("marc@test.fr", 50L))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("une reservation inexistante renvoie 404")
        void annulation_inexistante() {
            when(reservationRepo.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancel("marc@test.fr", 404L))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("une reservation confirmee peut encore etre annulee")
        void annulation_depuisConfirmee() {
            var r = reservationDe(client, StatutReservation.CONFIRMEE);
            when(reservationRepo.findById(50L)).thenReturn(Optional.of(r));

            service.cancel("marc@test.fr", 50L);

            assertThat(r.getStatut()).isEqualTo(StatutReservation.ANNULEE);
        }
    }
}
