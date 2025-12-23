import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {filter, map, shareReplay, switchMap, take, tap} from 'rxjs/operators';
import {BehaviorSubject, combineLatest, Observable, Subject} from 'rxjs';
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
import {
  EntretienEditDialogComponent
} from "../../features/entretien/entretien-edit-dialog/entretien-edit-dialog.component";
import {
  MouvementEditDialogComponent
} from "../../features/mouvement/mouvement-edit-dialog/mouvement-edit-dialog.component";
import {
  DocumentUploadDialogComponent
} from "../../features/document/document-upload-dialog/document-upload-dialog.component";
import {DocumentEditDialogComponent} from "../../features/document/document-edit-dialog/document-edit-dialog-component";
import {AuthService} from "../../../services/auth.service";
import {VenteService} from "../../../services/vente.service";
import {Vente} from "../../../models/vente";

@Component({
  selector: 'app-voiture-detail',
  templateUrl: './voiture-detail.component.html',
  styleUrls: ['./voiture-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VoitureDetailComponent implements OnInit {
  isEditing = false;
  fournisseurLibelle?: string;
  marqueLabelle?: string;
  modeleLabelle?: string;
  fournisseurs: Fournisseur[] = [];
  marques: Marque[] = [];
  modeles: Modele[] = [];
  idVoiture?: number;
  totalCoutEntretien?: number;
  ownerId?: number;
  entretiensList: Entretien[];
  currentUserId = this.authSrv.getUserIdFromToken();
  vente: Vente;
  benefice: number;

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
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  readonly entretiens$: Observable<Entretien[]> = combineLatest([
    this.id$,
    this.refresh$
  ]).pipe(
    switchMap(([id]) => this.eSrv.listByVoiture(id)),
    tap(entretiens => {
      if (entretiens) {
        this.calcultotalCoutEntrtien(entretiens);
        console.log("Entretien", entretiens);
        this.entretiensList = entretiens;
      }
    }),
    shareReplay(1)
  );

  // donnée principale
  // @ts-ignore
  readonly v$: Observable<VoitureDetailDto> = combineLatest([this.id$, this.entretiens$, this.refresh$]).pipe(
    switchMap(([id]) => this.vSrv.getById(id)),
    switchMap(voiture => this.marqueSrv.list().pipe(
      map(marques => {
        // Retourner l'objet voiture original mais avec les marques disponibles
        this.marques = marques;
        this.idVoiture = voiture.id;
        this.marqueLabelle = this.marques.find(item => item.id === voiture.idMarque).nom;
        return voiture;
      })
    )),
    tap((voiture) => {

      this.ownerId = voiture.owner;
      this.vtSrv.getVenteByIdVoiture(voiture.id).subscribe(vente => {
        if (vente) {
          console.log("Vente", vente);
          this.vente = vente;
          this.benefice = voiture.prixVente - (this.totalCoutEntretien + voiture.prixAchat);

        }
      });

      this.form = this.fb.group({
        idMarque: [voiture.idMarque ? voiture.idMarque : null, [Validators.required]],
        idModele: [voiture.idModele ? voiture.idModele : null, [Validators.required]],
        annee: [voiture.annee ? voiture.annee : null, [Validators.required, Validators.min(1900), Validators.max(new Date().getFullYear() + 1)]],
        prixVente: [voiture.prixVente ? voiture.prixVente : null, [Validators.min(0)]],
        prixAchat: [voiture.prixAchat ? voiture.prixAchat : null, [Validators.required, Validators.min(0)]],
        vin: [voiture.vin ? voiture.vin : ''],
        couleur: [voiture.couleur ? voiture.couleur : ''],
        kilometrage: [voiture.kilometrage ? voiture.kilometrage : null, [Validators.min(0)]],
        statut: <StatutVoiture | null>(voiture.statut ? voiture.statut : ''),
        idFournisseur: [voiture.idFournisseur ? voiture.idFournisseur : null, [Validators.min(1)]],
      });

      if (voiture.idMarque) {
        this.modeleSrv.listByMarque(voiture.idMarque).subscribe(ms => {
          this.modeles = ms;
          this.modeleLabelle = this.modeles.find(item => item.id === voiture.idModele).nom;
        });
      }

      if (voiture?.idFournisseur) {
        this.loadFournisseur(voiture.idFournisseur);
      }
    }),
    shareReplay(1)
  );

  // onglets enfants (chargés à la volée avec l’id)
  readonly documents$: Observable<Document[]> = combineLatest([this.id$, this.refresh$]).pipe(
    switchMap(([id]) => this.dSrv.listByVoiture(id)),
    shareReplay(1)
  );

  readonly ventes$: Observable<Vente> = combineLatest([this.id$, this.refresh$]).pipe(
    switchMap(([id]) =>
      this.vtSrv.getVenteByIdVoiture(id)),
        tap(vente => {
          if(vente){
            console.log("Vente", vente);
            // this.vente = vente;
          }
        }),
    shareReplay(1)
  );

  calcultotalCoutEntrtien(entretiens: Entretien[]) {
    this.totalCoutEntretien = 0;
    for (let entretien of entretiens) {
      this.totalCoutEntretien += entretien.cout;
    }
  }

  readonly mouvements$: Observable<Mouvement[]> = combineLatest([this.id$, this.refresh$]).pipe(
    switchMap(([id]) => this.mSrv.listByVoiture(id)),
    shareReplay(1)
  );

  constructor(
    private route: ActivatedRoute,
    private vSrv: VoitureService,
    private dSrv: DocumentService,
    private vtSrv: VenteService,
    private eSrv: EntretienService,
    private mSrv: MouvementService,
    private fSrv: FournisseurService,
    private authSrv: AuthService,
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


  get isOwner(): boolean {
    return !!this.ownerId && !!this.currentUserId && this.ownerId === this.currentUserId;
  }

  // === DOCUMENTS ===
  openEditDocDialog(d: Document) {
    const dialogRef = this.dialog.open(DocumentEditDialogComponent, {
      width: '520px',
      data: {doc: d}
    });
    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => this.refresh$.next());
  }

  deleteDoc(d: Document) {
    if (!confirm(`Supprimer le document "${d.nomFichier}" ?`)) return;
    this.dSrv.delete(d.id).subscribe(() => this.refresh$.next());
  }

  supprimerDocument(d: Document
  ) {
    if (!confirm('Supprimer ce document ?')) return;
    this.dSrv.delete(d.id).subscribe(() => this.refresh$.next());
  }

  download(d: Document
  ) {
    this.dSrv.download(d.id).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = d.nomFichier;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  // === ENTRETIENS ===
  planifierEntretien(entretien ?: Entretien) {
    const ref = this.dialog.open(EntretienEditDialogComponent, {
      width: '720px',
      data: {idVoiture: this.idVoiture, entretien}
    });
    ref.afterClosed().subscribe
    ({
      next: () => this.refresh$.next(),   // <-- recharge les 3 listes
      error: console.error
    })
  }

  supprimerEntretien(e: Entretien) {
    if (!confirm('Supprimer cet entretien ?')) return;
    this.eSrv.delete(e.id!).subscribe(() => this.refresh$.next());
  }

  // === MOUVEMENTS ===
  nouveauMouvement(mvt ?: Mouvement) {
    const ref = this.dialog.open(MouvementEditDialogComponent, {
      width: '720px',
      data: {idVoiture: this.id$, mvt}
    });
    ref.afterClosed().subscribe(res => {
      if (res) this.refresh$.next();
    });
  }

  supprimerMouvement(m: Mouvement) {
    if (!confirm('Supprimer ce mouvement ?')) return;
    this.mSrv.delete(m.id!).subscribe(() => this.refresh$.next());
  }

  edit() {
    this.isEditing = true;
  }

  openUploadDialog() {
    const dialogRef = this.dialog.open(DocumentUploadDialogComponent, {
      width: '520px',
      data: {idVoiture: this.idVoiture}
    });
    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => this.refresh$.next());
  }

  nouvelleVente(): void {
    this.v$               // Observable<VoitureDetailDto>
      .pipe(
        take(1),          // ✅ prend 1 valeur et se désabonne (plus besoin de 'sub')
        switchMap(v => {
          const ref = this.dialog.open(VenteCreateDialogComponent, {
            width: '720px',
            data: {
              idVoiture: v.id,
              idMarque: v.idMarque,
              idModele: v.idModele,
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
          // this.vSrv.getById(this.id$).subscribe(v => this.vSubject.next(v)); // si tu as un subject
          // // this.reloadChildren();
          //
          // // OU petit trick de navigation pour forcer un refresh d’Angular :
          // this.router.navigateByUrl('/', { skipLocationChange: true })
          //   .then(() => this.router.navigate(['/voitures', this.id]));
          //
          // this.router.navigateByUrl('/', {skipLocationChange: true}).then(() =>
          //   this.router.navigate(['/voitures', v.id])
          // );
        }
      });
  }

  cancel() {
    this.isEditing = false;
  }

  save(id
       :
       number
  ) {
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

  private loadFournisseur(id: number
  ) {
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

}
