import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {Client} from "../../../../models/client";
import {User} from "../../../../models/User";
import {VenteCreateDto} from "../../../../models/VenteCreateDto";
import {VenteService} from "../../../../services/vente.service";
import {UserService} from "../../../../services/user.service";
import {ClientService} from "../../../../services/client.service";
import {MethodePaiement} from "../../../../models/enums/MethodePaiement";

export interface VenteDialogData {
  idVoiture: number;
  prixSuggere?: number; // ex: prixVente actuel
  vendeurCourantId?: number; // si tu stockes l'utilisateur connecté
}

@Component({
  selector: 'app-vente-create-dialog',
  templateUrl: './vente-create-dialog.component.html',
  styleUrls: ['./vente-create-dialog.component.scss']
})
export class VenteCreateDialogComponent implements OnInit {
  form: FormGroup;
  clients: Client[] = [];
  vendeurs: User[] = [];
  loading = false;

  modes: { value: MethodePaiement; label: string }[] = [
    { value: 'ESPECES',  label: 'Espèces' },
    { value: 'CARTE',    label: 'Carte' },
    { value: 'VIREMENT', label: 'Virement' },
    { value: 'CHEQUE',   label: 'Chèque' }
  ];

  constructor(
    private fb: FormBuilder,
    private ventes: VenteService,
    private clientsSrv: ClientService,
    private usersSrv: UserService,
    private ref: MatDialogRef<VenteCreateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: VenteDialogData
  ) {
    this.form = this.fb.group({
      idClient: [null, Validators.required],
      idVendeur: [data.vendeurCourantId ?? null, Validators.required],
      dateVente: [new Date().toISOString().substring(0,10), Validators.required], // yyyy-MM-dd
      prixFinal: [data.prixSuggere ?? null, [Validators.required, Validators.min(0)]],
      modePaiement: ['ESPECES', Validators.required],
      acompteMontant: [null, [Validators.min(0)]],
    });
  }

  ngOnInit(): void {
    this.clientsSrv.list().subscribe(c => this.clients = c);
    this.usersSrv.list().subscribe(u => this.vendeurs = u);
  }

  save() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;

    const dto: VenteCreateDto = {
      idVoiture: this.data.idVoiture,
      idClient: this.form.value.idClient,
      idVendeur: this.form.value.idVendeur,
      dateVente: this.form.value.dateVente,  // backend peut accepter 'YYYY-MM-DD'
      prixFinal: this.form.value.prixFinal,
      modePaiement: this.form.value.modePaiement,
      acompteMontant: this.form.value.acompteMontant ?? undefined
    };

    this.ventes.create(dto).subscribe({
      next: v => { this.loading = false; this.ref.close(v); },
      error: _ => { this.loading = false; this.form.setErrors({ api: true }); }
    });
  }

  cancel() { this.ref.close(); }
}
