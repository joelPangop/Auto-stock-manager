import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Fournisseur} from "../../../../models/fournisseur";
import {StatutVoiture} from "../../../../models/enums/StatutVoiture";
import {VoitureService} from "../../../../services/voiture.service";
import {FournisseurService} from "../../../../services/fournisseur.service";
import {VoitureCreateDto} from "../../../../models/VoitureCreateDto";
import {Modele} from "../../../../models/modele";
import {Marque} from "../../../../models/marque";
import {ModeleService} from "../../../../services/modele.service";
import {MarqueService} from "../../../../services/marque.service";
import {MarqueCreateDialogComponent} from "../../catalog/marque-create-dialog/marque-create-dialog.component";
import {ModeleCreateDialogComponent} from "../../catalog/modele-create-dialog/modele-create-dialog.component";
import {take} from "rxjs/operators";
import {
  FournisseurCreateDialogComponent
} from "../../fournisseur/fournisseur-create-dialog/fournisseur-create-dialog.component";

@Component({
  selector: 'app-voiture-create-dialog',
  templateUrl: './voiture-create-dialog.component.html',
  styleUrls: ['./voiture-create-dialog.component.scss']
})
export class VoitureCreateDialogComponent implements OnInit {

  readonly NEW_SUPPLIER = -1;

  form: FormGroup;
  fournisseurs: Fournisseur[] = [];
  loading = false;
  marques: Marque[] = [];
  modeles: Modele[] = [];

  statuts: StatutVoiture[] = ['EN_STOCK','DISPONIBLE','RESERVEE','VENDUE'];

  constructor(
    private fb: FormBuilder,
    private voitures: VoitureService,
    private fournisseursSrv: FournisseurService,
    private ref: MatDialogRef<VoitureCreateDialogComponent>,
    private dlg: MatDialog,
    private marqueSrv: MarqueService,
    private modeleSrv: ModeleService,
  ) {
    this.form = this.fb.group({
      idMarque: [null, [Validators.required]],
      idModele: [null, [Validators.required]],
      annee: [new Date().getFullYear(), [Validators.required, Validators.min(1900), Validators.max(new Date().getFullYear()+1)]],
      vin: [''],
      couleur: [''],
      kilometrage: [null, [Validators.min(0)]],
      prixAchat: [null, [Validators.min(0)]],
      prixVente: [null, [Validators.min(0)]],
      statut: ['EN_STOCK' as StatutVoiture, Validators.required],
      idFournisseur: [null],
      dateEntreeStock: [new Date().toISOString().substring(0,10)],
      creerMouvementEntree: [true]
    });
  }

  ngOnInit(): void {

    this.marqueSrv.list().subscribe(m => this.marques = m);

    this.form.get('idMarque')!.valueChanges.subscribe((idMarque: number | null) => {
      this.form.get('idModele')!.setValue(null, { emitEvent: false });
      this.modeles = [];
      if (idMarque) {
        this.modeleSrv.listByMarque(idMarque).subscribe(ms => this.modeles = ms);
      }
    });

    this.fournisseursSrv.listAll().subscribe({
      next: f => this.fournisseurs = f,
      error: e => console.error('Erreur chargement fournisseurs', e)
    });

    this.form.get('idFournisseur')!.valueChanges.subscribe(val => {
      if (val === this.NEW_SUPPLIER) {
        this.openNewFournisseur();
      }
    });
  }

  save() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    const dto: VoitureCreateDto = this.form.value;
    this.voitures.create(dto).subscribe({
      next: v => { this.loading = false; this.ref.close(v); },
      error: _ => { this.loading = false; this.form.setErrors({ api: true }); }
    });
  }

  cancel() { this.ref.close(); }

  addMarque() {
    const ref = this.dlg.open(MarqueCreateDialogComponent, { width: '420px' });
    ref.afterClosed().subscribe((created?: Marque) => {
      if (created) {
        this.marques = [created, ...this.marques];
        this.form.get('idMarque')!.setValue(created.id);
      }
    });
  }

  addModele() {
    const idMarque = this.form.value.idMarque;
    if (!idMarque) { return; }
    const ref = this.dlg.open(ModeleCreateDialogComponent, { width: '420px', data: { idMarque } });
    ref.afterClosed().subscribe((created?: Modele) => {
      if (created) {
        this.modeles = [created, ...this.modeles];
        this.form.get('idModele')!.setValue(created.id);
      }
    });
  }

  private openNewFournisseur() {
    const ref = this.dlg.open(FournisseurCreateDialogComponent, { width: '640px' });
    ref.afterClosed().pipe(take(1)).subscribe((created?: Fournisseur) => {
      if (!created) {
        // remettre la valeur à null si l'utilisateur annule
        this.form.patchValue({ idFournisseur: null }, { emitEvent: false });
        return;
      }
      // rafraîchir la liste et sélectionner le nouveau
      const list = [created, ...this.fournisseurs].sort((a,b)=>a.nom.localeCompare(b.nom));
      this.fournisseurs = list;
      this.form.patchValue({ idFournisseur: created.id }, { emitEvent: false });
    });
  }
}
