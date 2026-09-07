import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {EntretienService} from "../../../../services/entretien.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Entretien} from "../../../../models/entretien";

@Component({
  selector: 'app-entretien-edit-dialog',
  templateUrl: './entretien-edit-dialog.component.html',
  styleUrls: ['./entretien-edit-dialog.component.scss']
})
export class EntretienEditDialogComponent implements OnInit {

  form: FormGroup;
  apiError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private srv: EntretienService,
    private ref: MatDialogRef<EntretienEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { idVoiture: number, entretien?: Entretien }
  ) {
    this.form = this.fb.group({
      idVoiture: [this.data.idVoiture, [Validators.required]],
      type: [this.data?.entretien?.type ?? 'VIDANGE', [Validators.required]],
      dateEntretien: [this.toDateString(this.data?.entretien?.dateEntretien), [Validators.required]],
      cout: [this.data?.entretien?.cout ?? null, [Validators.required]],
      commentaire: [this.data?.entretien?.commentaire ?? null]
    });
  }

  ngOnInit(): void {}

  save(): void {
    if (this.form.invalid) { return; }
    this.apiError = null;
    const raw = this.form.value as Entretien;
    const dateStr: string = raw.dateEntretien as any;
    const dto = {
      ...raw,
      dateEntretien: dateStr.length === 10 ? dateStr + 'T00:00:00' : dateStr.slice(0, 19)
    };
    const requete = this.data.entretien?.id
      ? this.srv.update(this.data.entretien.id!, dto)
      : this.srv.create(dto);

    // Sans branche d'erreur, un echec laissait la boite ouverte sans rien dire.
    requete.subscribe({
      next: res => this.ref.close(res),
      error: err => this.apiError =
        err?.error?.message ?? "L'enregistrement de l'entretien a echoue."
    });
  }

  private toDateString(val: any): string {
    if (!val) { return new Date().toISOString().slice(0, 10); }
    const s = String(val);
    // Already yyyy-MM-dd
    if (s.length === 10) { return s; }
    // ISO datetime: take first 10 chars
    return s.slice(0, 10);
  }

}
