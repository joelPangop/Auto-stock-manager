import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {map, shareReplay, switchMap, take, tap} from 'rxjs/operators';
import {Observable} from 'rxjs';
import {VoitureDetailDto} from '../../../models/VoitureDetailDto';
import {Document} from '../../../models/document';
import {Entretien} from '../../../models/entretien';
import {Mouvement} from '../../../models/mouvement';
import {VoitureService} from '../../../services/voiture.service';
import {DocumentService} from '../../../services/document.service';
import {EntretienService} from '../../../services/entretien.service';
import {MouvementService} from '../../../services/mouvement.service';
import {FournisseurService} from "../../../services/fournisseur.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {StatutVoiture} from "../../../models/enums/StatutVoiture";
import {MatSnackBar} from "@angular/material/snack-bar";
import {Fournisseur} from "../../../models/fournisseur";
import {MatDialog} from "@angular/material/dialog";
import {VenteCreateDialogComponent} from "../../features/ventes/vente-create-dialog/vente-create-dialog.component";
import {Marque} from "../../../models/marque";
import {Modele} from "../../../models/modele";
import {MarqueService} from "../../../services/marque.service";
import {ModeleService} from "../../../services/modele.service";

@Component({
  selector: 'app-voiture-detail',
  templateUrl: './voiture-detail.component.html',
  styleUrls: ['./voiture-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VoitureDetailComponent implements OnInit {
  isEditing = false;
  fournisseurLibelle?: string;
  marqueLibelle?: string;
  modeleLibelle?: string;
  fournisseurs: Fournisseur[] = [];
  marques: Marque[] = [];
  modeles: Modele[] = [];

  // id$ réagit à chaque changement d’URL
  readonly id$ = this.route.paramMap.pipe(
    map(pm => Number(pm.get('id')))
  );

  ngOnInit(): void {
    // charge les fournisseurs au démarrage
    this.fSrv.listAll().subscribe({
      next: f => (this.fournisseurs = f),
      error: e => console.error('Erreur chargement fournisseurs', e)
    });
  }

  form: FormGroup;
  // donnée principale
  // @ts-ignore
  readonly v$: Observable<VoitureDetailDto> = this.id$.pipe(
    switchMap(id => this.vSrv.getById(id)),
    switchMap(voiture => this.marqueSrv.list().pipe(
      map(marques => {
        // Retourner l'objet voiture original mais avec les marques disponibles
        this.marques = marques;
        this.marqueLibelle = this.marques.find(item => item.id === voiture.idMarque).nom;
        return voiture;
      })
    )),
    tap((voiture) => {

      this.form = this.fb.group({
        idMarque: [voiture.idMarque ? voiture.idMarque : null, [Validators.required]],
        idModele: [voiture.idModele ? voiture.idModele : null, [Validators.required]],
        annee: [voiture.annee ? voiture.annee : null, [Validators.required, Validators.min(1900), Validators.max(new Date().getFullYear() + 1)]],
        prixVente: [voiture.prixVente ? voiture.prixVente : null, [Validators.required, Validators.min(0)]],
        prixAchat: [voiture.prixAchat ? voiture.prixAchat : null, [Validators.required, Validators.min(0)]],
        vin: [voiture.vin ? voiture.vin : ''],
        couleur: [voiture.couleur ? voiture.couleur : ''],
        kilometrage: [voiture.kilometrage ? voiture.kilometrage : null, [Validators.min(0)]],
        statut: <StatutVoiture | null>(voiture.statut ? voiture.statut : ''),
        idFournisseur: [voiture.idFournisseur ? voiture.idFournisseur : null, [Validators.min(1)]],
      });

      if (voiture.idMarque) {
        this.modeleSrv.listByMarque(voiture.idMarque).subscribe(ms =>  {
          this.modeles = ms;
          this.modeleLibelle = this.modeles.find(item => item.id === voiture.idModele).nom;
        });
      }

      if (voiture?.idFournisseur) {
        this.loadFournisseur(voiture.idFournisseur);
      }
    }),
    shareReplay(1)
  );

  // onglets enfants (chargés à la volée avec l’id)
  readonly documents$: Observable<Document[]> = this.id$.pipe(
    switchMap(id => this.dSrv.listByVoiture(id)),
    shareReplay(1)
  );

  readonly entretiens$: Observable<Entretien[]> = this.id$.pipe(
    switchMap(id => this.eSrv.listByVoiture(id)),
    tap(entretien => {
      if (entretien) {
        console.log("Entretien", entretien);
      }
    }),
    shareReplay(1)
  );

  readonly mouvements$: Observable<Mouvement[]> = this.id$.pipe(
    switchMap(id => this.mSrv.listByVoiture(id)),
    shareReplay(1)
  );


  // form: FormGroup = this.fb.group({
  //   marque: [ this.voitureDetail ? this.voitureDetail.marque : 'test', [Validators.required, Validators.maxLength(60)]],
  //   modele: ['', [Validators.required, Validators.maxLength(60)]],
  //   annee: [null, [Validators.required, Validators.min(1900), Validators.max(new Date().getFullYear() + 1)]],
  //   prixVente: [null, [Validators.required, Validators.min(0)]],
  //   vin: [''],
  //   couleur: [''],
  //   kilometrage: [null, [Validators.min(0)]],
  //   statut: <StatutVoiture | null> (null),
  //   idFournisseur: [null, [Validators.min(1)]],
  // });

  constructor(
    private route: ActivatedRoute,
    private vSrv: VoitureService,
    private dSrv: DocumentService,
    private eSrv: EntretienService,
    private mSrv: MouvementService,
    private fSrv: FournisseurService,
    private fb: FormBuilder,
    private snack: MatSnackBar,
    private dialog: MatDialog,
    private router: Router,
    private marqueSrv: MarqueService,
    private modeleSrv: ModeleService
  ) {
  }

  /** Alimente le formulaire quand la voiture est chargée */
  private patchForm(v: VoitureDetailDto) {
    this.form.patchValue({
      idMarque: v.idMarque ?? null,
      idModele: v.idModele ?? null,
      annee: v.annee,
      prixVente: (v as any).prixVente ?? (v as any).prix ?? null,
      vin: v.vin ?? '',
      couleur: v.couleur ?? '',
      kilometrage: v.kilometrage ?? null,
      statut: (v as any).etat ?? v.statut,   // selon ton DTO
      idFournisseur: v.idFournisseur ?? null
    }, {emitEvent: false});
  }

  // actions (restent inchangées)
  ajouterDocument() {
  }

  planifierEntretien() {
  }

  nouveauMouvement() {
  }

  edit() {
    this.isEditing = true;
  }
  //
  // nouvelleVente() {
  //   // récupère la voiture en cours pour prix suggéré
  //   const sub = this.v$.subscribe(v => {
  //     const ref = this.dialog.open(VenteCreateDialogComponent, {
  //       width: '720px',
  //       data: {
  //         idVoiture: v.id,
  //         prixSuggere: (v as any).prixVente ?? (v as any).prix ?? null,
  //         vendeurCourantId: undefined // ou l’id de l’utilisateur connecté si tu l’as
  //       }
  //     });
  //
  //     ref.afterClosed().subscribe(created => {
  //       if (created) {
  //         // possibilité : afficher un snack et rafraîchir la vue
  //         // -> recharger v$, enfants si la vente change le statut
  //         // Ici v$ est un Observable basé sur l’URL; pour forcer un refresh:
  //         this.router.navigateByUrl('/', {skipLocationChange: true}).then(() =>
  //           this.router.navigate(['/voitures', v.id])
  //         );
  //       }
  //     });
  //
  //     sub.unsubscribe();
  //   });
  // }


nouvelleVente(): void {
  this.v$               // Observable<VoitureDetailDto>
    .pipe(
      take(1),          // ✅ prend 1 valeur et se désabonne (plus besoin de 'sub')
      switchMap(v => {
        const ref = this.dialog.open(VenteCreateDialogComponent, {
          width: '720px',
          data: {
            idVoiture: v.id,
            idMarque:  v.idMarque,
            idModele:  v.idModele,
            idFournisseur: v.idFournisseur ?? null,
            prixSuggere: (v as any).prixVente ?? (v as any).prix ?? null,
            vendeurId: undefined // ou l'id de l'utilisateur connecté si tu l’as
          }
        });
        return ref.afterClosed(); // Observable<boolean | any>
      })
    )
    .subscribe((created) => {
      if (created) {
        // Rafraîchir la fiche + ses onglets
        this.vSrv.getById(this.id).subscribe(v => this.vSubject.next(v)); // si tu as un subject
        this.reloadChildren();

        // OU petit trick de navigation pour forcer un refresh d’Angular :
        this.router.navigateByUrl('/', { skipLocationChange: true })
          .then(() => this.router.navigate(['/voitures', this.id]));

        this.router.navigateByUrl('/', {skipLocationChange: true}).then(() =>
          this.router.navigate(['/voitures', v.id])
        );
      }
    });
}


cancel() {
    this.isEditing = false;
  }

  download(d: Document) {
    this.dSrv.download(d.id).subscribe(); // ou déclenche un lien <a>
  }

  save(id: number) {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const payload = this.form.value;
    console.log("payload", payload);
    this.vSrv.update(id, payload).subscribe({
      next: updated => {
        this.isEditing = false;
        this.patchForm(updated);
        this.snack.open('Voiture mise à jour ✔', 'OK', {duration: 2000});
      },
      error: err => {
        console.error(err);
        this.snack.open('Échec de la mise à jour', 'Fermer', {duration: 3000});
      }
    });
  }

  private loadFournisseur(id: number) {
    this.fSrv.getById(id).subscribe({
      next: (f: any) => {
        // adapte selon le DTO de ton service fournisseurs
        this.fournisseurLibelle = f?.nom ? `${f.nom} (#${id})` : `#${id}`;
      },
      error: () => {
        this.fournisseurLibelle = `#${id}`;
      }
    });
  }

  protected readonly status = status;
}
