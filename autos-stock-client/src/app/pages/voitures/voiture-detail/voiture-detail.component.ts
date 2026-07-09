import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {filter, map, shareReplay, switchMap, take, tap} from 'rxjs/operators';
import {BehaviorSubject, combineLatest, forkJoin, Observable, Subject, Subscription} from 'rxjs';
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
import {PaiementService} from "../../../services/paiement.service";

@Component({
  selector: 'app-voiture-detail',
  templateUrl: './voiture-detail.component.html',
  styleUrls: ['./voiture-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VoitureDetailComponent implements OnInit, OnDestroy {
  isEditing = false;
  isSaving  = false;
  fournisseurLibelle?: string;
  paiementLibelle?: string;
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

  // === PHOTOS ===
  photoUrlMap: { [id: number]: string } = {};
  lightboxPhoto: Document | null = null;
  private _photoSub?: Subscription;

  // === SÉLECTION MULTIPLE ===
  selectedPhotoIds = new Set<number>();

  isPhotoSelected(id: number): boolean {
    return this.selectedPhotoIds.has(id);
  }

  togglePhotoSelection(id: number): void {
    if (this.selectedPhotoIds.has(id)) {
      this.selectedPhotoIds.delete(id);
    } else {
      this.selectedPhotoIds.add(id);
    }
    this.cdr.markForCheck();
  }

  toggleSelectAll(photos: Document[]): void {
    if (this.selectedPhotoIds.size === photos.length) {
      this.selectedPhotoIds.clear();
    } else {
      photos.forEach(p => this.selectedPhotoIds.add(p.id));
    }
    this.cdr.markForCheck();
  }

  clearSelection(): void {
    this.selectedPhotoIds.clear();
    this.cdr.markForCheck();
  }

  deleteSelectedPhotos(): void {
    const count = this.selectedPhotoIds.size;
    if (count === 0) return;
    if (!confirm(`Supprimer ${count} photo(s) sélectionnée(s) ?`)) return;

    const deletes$ = Array.from(this.selectedPhotoIds).map(id => this.dSrv.delete(id));
    forkJoin(deletes$).subscribe({
      next: () => {
        this.snack.open(`${count} photo(s) supprimée(s) ✔`, 'OK', { duration: 2500 });
        this.selectedPhotoIds.clear();
        this.refresh$.next();
      },
      error: () => this.snack.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 })
    });
  }

  // id$ réagit à chaque changement d’URL
  readonly id$ = this.route.paramMap.pipe(
    map(pm => Number(pm.get('id')))
  );

  ngOnInit(): void {
    this.fSrv.listAll().subscribe({
      next: f => (this.fournisseurs = f),
      error: e => console.error('Erreur chargement fournisseurs', e)
    });
    this._photoSub = this.photos$.subscribe(photos => this.loadPhotoUrls(photos));
  }

  ngOnDestroy(): void {
    this._photoSub?.unsubscribe();
  }

  form: FormGroup;
  private readonly refresh$ = new BehaviorSubject<void>(undefined);
  readonly refreshTrigger$ = this.refresh$.asObservable();

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

      this.vtSrv.getVenteByIdVoiture(voiture.id).subscribe({
        next: (vente) => {
          if (vente) {
            console.log("Vente", vente);
            this.vente = vente;
            this.pSrv.listByVente(vente.id).subscribe({
              next: (paiements) => {
                if (paiements.length > 0) {
                  console.log("Paiements", paiements);
                  this.paiementLibelle = paiements[0].methode;
                  let paie = paiements[0].methode;
                  for (let p of paiements) {
                    if (paie !== p.methode) {
                      this.paiementLibelle += p.methode + " ";
                      paie = p.methode;
                    }
                  }
                }
              }
            })
            this.benefice = voiture.prixVente - (this.totalCoutEntretien + voiture.prixAchat);
          }
        }, error: (err: any) => {
          const msg = err?.error?.message ?? err?.message ?? 'Erreur inconnue';
          console.log(msg);
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
        categorie: [voiture.categorie ?? null],
        idFournisseur: [voiture.idFournisseur ? voiture.idFournisseur : null, [Validators.min(1)]],
        description: [voiture.description ?? '', [Validators.maxLength(5000)]],
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
  private readonly allDocs$: Observable<Document[]> = combineLatest([this.id$, this.refresh$]).pipe(
    switchMap(([id]) => this.dSrv.listByVoiture(id)),
    shareReplay(1)
  );

  readonly documents$: Observable<Document[]> = this.allDocs$.pipe(
    map(docs => docs.filter(d => d.type !== ‘PHOTO’))
  );

  readonly photos$: Observable<Document[]> = this.allDocs$.pipe(
    map(docs => docs.filter(d => d.type === ‘PHOTO’))
  );

  readonly ventes$: Observable<Vente> = combineLatest([this.id$, this.refresh$]).pipe(
    switchMap(([id]) =>
      this.vtSrv.getVenteByIdVoiture(id)),
    tap(vente => {
      if (vente) {
        console.log("Vente", vente);
        // this.vente = vente;
      }
    }),
    shareReplay(1)
  );

  triggerRefresh() {
    this.refresh$.next(undefined);
  }

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
    private pSrv: PaiementService,
    private eSrv: EntretienService,
    private mSrv: MouvementService,
    private fSrv: FournisseurService,
    private authSrv: AuthService,
    private fb: FormBuilder,
    private snack: MatSnackBar,
    private dialog: MatDialog,
    private router: Router,
    private marqueSrv: MarqueService,
    private modeleSrv: ModeleService,
    private cdr: ChangeDetectorRef
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
      statut: (v as any).etat ?? v.statut,
      categorie: v.categorie ?? null,
      idFournisseur: v.idFournisseur ?? null
    }, {emitEvent: false});
  }


  get isOwner(): boolean {
    return this.authSrv.isAdmin() || (!!this.ownerId && !!this.currentUserId && this.ownerId === this.currentUserId);
  }

  // === DOCUMENTS ===
  openEditDocDialog(d: Document) {
    const dialogRef = this.dialog.open(DocumentEditDialogComponent, {
      disableClose: true,
      width: '520px',
      data: {doc: d}
    });
    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(
      () => {
        this.triggerRefresh();
        this.refresh$.next()
      }
    );
  }

  deleteDoc(d: Document) {
    if (!confirm(`Supprimer le document "${d.nomFichier}" ?`)) return;
    this.dSrv.delete(d.id).subscribe(() => {
      this.triggerRefresh()
      this.refresh$.next()
    });
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
    const ref = this.dialog.open(EntretienEditDialogComponent,
      {
        disableClose: true,
        width: '720px',
        data: {idVoiture: this.idVoiture, entretien}
      });
    ref.afterClosed().subscribe
    ({
      next: () => {
        this.triggerRefresh()
        this.refresh$.next()
      },   // <-- recharge les 3 listes
      error: console.error
    })
  }

  supprimerEntretien(e: Entretien) {
    if (!confirm('Supprimer cet entretien ?')) return;
    this.eSrv.delete(e.id!).subscribe(() => this.refresh$.next());
  }

  // === MOUVEMENTS ===
  nouveauMouvement(mvt ?: Mouvement) {
    const ref = this.dialog.open(MouvementEditDialogComponent,
      {
        disableClose: true,
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
    const dialogRef = this.dialog.open(DocumentUploadDialogComponent,
      {
        disableClose: true,
        width: '520px',
        data: {idVoiture: this.idVoiture}
      });
    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => this.refresh$.next());
  }

  openPhotoUploadDialog(): void {
    const dialogRef = this.dialog.open(DocumentUploadDialogComponent, {
      disableClose: true,
      width: '520px',
      data: {idVoiture: this.idVoiture, defaultType: 'PHOTO'}
    });
    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => this.refresh$.next());
  }

  setPhotoPrincipale(p: Document): void {
    this.dSrv.setPhotoPrincipale(p.id).subscribe({
      next: () => {
        this.snack.open('Photo principale définie ✔', 'OK', { duration: 2000 });
        this.refresh$.next();
      },
      error: () => this.snack.open('Erreur lors de la mise à jour', 'Fermer', { duration: 3000 })
    });
  }

  openPhotoFull(p: Document): void {
    this.lightboxPhoto = p;
  }

  closeLightbox(): void {
    this.lightboxPhoto = null;
  }

  private loadPhotoUrls(photos: Document[]): void {
    this.photoUrlMap = {};
    this.cdr.markForCheck();
    photos.forEach(photo => {
      this.dSrv.download(photo.id).subscribe({
        next: blob => {
          const reader = new FileReader();
          reader.onloadend = () => {
            this.photoUrlMap = {
              ...this.photoUrlMap,
              [photo.id]: reader.result as string
            };
            this.cdr.markForCheck();
          };
          reader.readAsDataURL(blob);
        },
        error: err => console.error(`Erreur chargement photo ${photo.id}`, err)
      });
    });
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

  save(id: number) {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isSaving = true;
    this.cdr.markForCheck();

    const payload = this.form.value;
    this.vSrv.update(id, payload).subscribe({
      next: () => {
        // Rafraîchit tout le pipeline v$ → recharge les données depuis le backend
        this.refresh$.next();
        this.isEditing = false;
        this.isSaving  = false;
        this.cdr.markForCheck();
        this.snack.open('Voiture mise à jour ✔', 'OK', {duration: 2000});
      },
      error: err => {
        console.error(err);
        this.isSaving = false;
        this.cdr.markForCheck();
        this.snack.open('Échec de la mise à jour', 'Fermer', {duration: 3000});
      }
    });
  }

  private loadFournisseur(id: number
  ) {
    this.fSrv.getById(id).subscribe({
      next: (f: any) => {
        // adapte selon le DTO de ton service fournisseurs
        this.fournisseurLibelle = f?.nom ? `${f.nom} (${f.type})` : `#${id}`;
      },
      error: () => {
        this.fournisseurLibelle = `#${id}`;
      }
    });
  }

}
