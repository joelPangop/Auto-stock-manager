import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { VenteService } from '../../../../services/vente.service';
import { Vente } from '../../../../models/vente';
import { MethodePaiement } from '../../../../models/enums/MethodePaiement';
import { Client } from '../../../../models/client';
import { User } from '../../../../models/User';
import { ClientService } from '../../../../services/client.service';
import { UserService } from '../../../../services/user.service';

@Component({
  selector: 'app-vente-edit-dialog',
  templateUrl: './vente-edit-dialog.component.html',
})
export class VenteEditDialogComponent implements OnInit {
  form: FormGroup;
  loading = false;
  clients: Client[] = [];
  vendeurs: User[] = [];

  modes: { value: MethodePaiement; label: string }[] = [
    { value: 'ESPECES', label: 'Espèces' },
    { value: 'CARTE',   label: 'Carte' },
    { value: 'VIREMENT', label: 'Virement' },
    { value: 'CHEQUE',  label: 'Chèque' },
  ];

  constructor(
    private fb: FormBuilder,
    private venteSrv: VenteService,
    private clientSrv: ClientService,
    private userSrv: UserService,
    private ref: MatDialogRef<VenteEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public vente: Vente
  ) {
    this.form = this.fb.group({
      idClient:     [vente.clientId   ?? null, Validators.required],
      idVendeur:    [(vente as any).vendeurId ?? null, Validators.required],
      dateVente:    [vente.dateVente   ?? new Date().toISOString().substring(0, 10), Validators.required],
      prixFinal:    [vente.prixFinal   ?? null, [Validators.required, Validators.min(0)]],
      modePaiement: [(vente as any).modePaiement ?? 'ESPECES', Validators.required],
    });
  }

  ngOnInit(): void {
    this.clientSrv.list().subscribe(c => this.clients = c);
    this.userSrv.list().subscribe(u => this.vendeurs = u);
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.venteSrv.update(this.vente.id, this.form.value).subscribe({
      next: updated => { this.loading = false; this.ref.close(updated); },
      error: () => { this.loading = false; }
    });
  }

  cancel(): void { this.ref.close(); }
}
