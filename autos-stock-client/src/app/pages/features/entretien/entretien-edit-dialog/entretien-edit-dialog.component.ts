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

  constructor(
    private fb: FormBuilder,
    private srv: EntretienService,
    private ref: MatDialogRef<EntretienEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { idVoiture: number, entretien?: Entretien }
  ) {
    this.form = this.fb.group({
      idVoiture: [this.data.idVoiture, [Validators.required]],
      type: [this.data?.entretien?.type ?? 'VIDANGE', [Validators.required]],
      dateEntretien: [this.data?.entretien?.dateEntretien ?? new Date().toISOString().slice(0, 10), [Validators.required]],
      cout: [this.data?.entretien?.cout ?? null, [Validators.required]],
      commentaire: [this.data?.entretien?.commentaire ?? null]
    });
  }

  ngOnInit(): void {

  }

  save() {
    const raw = this.form.value as Entretien;
    const dto = {
      ...raw,
      dateEntretien: raw.dateEntretien instanceof Date?
      raw.dateEntretien.toISOString().slice(0, 19) : (raw.dateEntretien + 'T00:00:00'),
    }
    if(this.data.entretien?.id){
      this.srv.update(this.data.entretien.id!, dto).subscribe(res =>{
        this.ref.close(res)
      })
    } else {
      this.srv.create(dto).subscribe(res =>{
        this.ref.close(res)
      })
    }
  }

}
