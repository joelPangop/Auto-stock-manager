import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {DepenseCreateDto} from "../../../../models/DepenseCreateDto";
import {DepenseService} from "../../../../services/depense.service";
import {BehaviorSubject} from "rxjs";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {
  FournisseurCreateDialogComponent
} from "../../fournisseur/fournisseur-create-dialog/fournisseur-create-dialog.component";
import {take} from "rxjs/operators";
import {Fournisseur} from "../../../../models/fournisseur";
import {FournisseurService} from "../../../../services/fournisseur.service";
import {CategorieDepense} from "../../../../models/enums/CategorieDepense";
import {DepenseDto} from "../../../../models/DepenseDto";

@Component({
  selector: 'app-voiture-depense-dialog',
  templateUrl: './voiture-depense-dialog.component.html',
  styleUrls: ['./voiture-depense-dialog.component.scss']
})
export class VoitureDepenseDialogComponent implements OnInit {
  private readonly refresh$ = new BehaviorSubject<void>(undefined);
  fournisseurs: Fournisseur[] = [];
  depenseForm: FormGroup;
  existingDepense: DepenseDto;
  showEdit: boolean = false;
  isNew: boolean = true;
  title: string = "Nouvelle depense";
  fournisseurLibelle?: string;

  readonly NEW_SUPPLIER = -1;

  typeDepensesLabels: Record<CategorieDepense, string> = {
    [CategorieDepense.ENTRETIEN]: 'Entretien',
    [CategorieDepense.ESSENCE]: 'Essence',
    [CategorieDepense.PARKING]: 'Parking',
    [CategorieDepense.ASSURANCE]: 'Assurance',
    [CategorieDepense.FRAIS_ADMIN]: 'Frais d\'administration',
    [CategorieDepense.AMENDE]: 'Amende',
    [CategorieDepense.AUTRE]: 'Autre'
  };

  typeDepenseOptions = Object.values(CategorieDepense);

  constructor(private fb: FormBuilder, private depSrv: DepenseService,
              private dialog: MatDialog,
              private fournisseursSrv: FournisseurService,
              private ref: MatDialogRef<VoitureDepenseDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    if (this.requireDepenseId()) {
      this.title = "Details Depense";
      this.depSrv.getDepense(this.requireVoitureId(), this.requireDepenseId()).subscribe(dep => {

        this.existingDepense = dep;
        this.depenseForm = this.fb.group({
          categorie: [dep.categorie, [Validators.required]],
          montant: [dep.montant, [Validators.required, Validators.min(0.01)]],
          dateDepense: [dep.dateDepense, [Validators.required]],
          description: [dep.description],
          fournisseurId: [dep.fournisseurId],
          documentId: [null as any],
          entretienId: [null as any],
        });

        this.fournisseursSrv.getById(dep.fournisseurId).subscribe({
          next: (f: any) => {
            // adapte selon le DTO de ton service fournisseurs
            this.fournisseurLibelle = f?.nom ? `${f.nom} (${f.type})` : `#${dep.fournisseurId}`;
          },
          error: () => {
            this.fournisseurLibelle = `#${dep.fournisseurId}`;
          }
        });
      });
    }
  }

  ngOnInit(): void {

    this.depenseForm = this.fb.group({
      categorie: ['ESSENCE', [Validators.required]],
      montant: [null as any, [Validators.required, Validators.min(0.01)]],
      dateDepense: ['', [Validators.required]],
      description: [''],
      fournisseurId: [null as any],
      documentId: [null as any],
      entretienId: [null as any],
    });

    this.fournisseursSrv.listAll().subscribe({
      next: f => this.fournisseurs = f,
      error: e => console.error('Erreur chargement fournisseurs', e)
    });

    this.depenseForm.get('fournisseurId')!.valueChanges.subscribe(val => {
      if (val === this.NEW_SUPPLIER) {
        this.openNewFournisseur();
      }
    });
  }

  create(): void {
    if (this.depenseForm.invalid) {
      this.depenseForm.markAllAsTouched();
      return;
    }
    const id = this.requireVoitureId();
    const depenseId = this.requireDepenseId();
    const dto = this.depenseForm.getRawValue() as DepenseCreateDto;

    if(depenseId){
      this.depSrv.update(id, depenseId, dto).subscribe({
        next: () => {
          // this.refresh();
          this.ref.close();
        },
        error: (err) => console.error(err)
      });
    } else {
      this.depSrv.create(id, dto).subscribe({
        next: () => {
          // this.refresh();
          this.ref.close();
        },
        error: (err) => console.error(err)
      });
    }
  }

  private requireVoitureId(): number {
    if (this.data.idVoiture == null) {
      // évite "undefined where a stream expected" + évite appels avant l'input
      throw new Error('voitureId est requis pour charger les dépenses.');
    }
    return Number(this.data.idVoiture);
  }

  private requireDepenseId(): number {
    if (!this.data.depenseId) {
      // évite "undefined where a stream expected" + évite appels avant l'input
      this.isNew = true;
      return null;
    } else this.isNew = false;
    return Number(this.data.depenseId);
  }

  refresh(): void {
    this.refresh$.next(undefined);
  }

  cancel() {
    this.ref.close();
  }

  private openNewFournisseur() {
    const ref = this.dialog.open(FournisseurCreateDialogComponent, {
      disableClose: true, width: '640px'
    });
    ref.afterClosed().pipe(take(1)).subscribe((created?: Fournisseur) => {
      if (!created) {
        // remettre la valeur à null si l'utilisateur annule
        this.depenseForm.patchValue({fournisseurId: null}, {emitEvent: false});
        return;
      }
      // rafraîchir la liste et sélectionner le nouveau
      const list = [created, ...this.fournisseurs].sort((a, b) => a.nom.localeCompare(b.nom));
      this.fournisseurs = list;
      this.depenseForm.patchValue({fournisseurId: created.id}, {emitEvent: false});
    });
  }

  setShowEdit(v: boolean): void {
    this.showEdit = v;
    this.title = v ? "Modifier depense": "Details depense";
  }

}
