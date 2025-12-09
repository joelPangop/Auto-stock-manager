import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MouvementService} from "../../../../services/mouvement.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Mouvement} from "../../../../models/mouvement";

@Component({
  selector: 'app-mouvement-edit-dialog',
  templateUrl: './mouvement-edit-dialog.component.html',
  styleUrls: ['./mouvement-edit-dialog.component.scss']
})
export class MouvementEditDialogComponent implements OnInit {
  form: FormGroup;
  constructor(  private fb: FormBuilder,
                private srv: MouvementService,
                private ref: MatDialogRef<MouvementEditDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: { idVoiture:number, mvt?: Mouvement }) {
    this.form = this.fb.group({
      idVoiture: [this.data.idVoiture, [Validators.required]],
      type: [this.data?.mvt?.type ?? 'ENTREE', [Validators.required]],
      dateMouvement: [this.data?.mvt?.dateMouvement ?? new Date().toISOString().slice(0,16), [Validators.required]],
      commentaire: [this.data?.mvt?.commentaire ?? null, [Validators.required]]
    });
  }

  ngOnInit(): void {
  }

  save() {
    const dto: Mouvement = this.form.value;
    const obs = this.data.mvt?.id ? this.srv.update(this.data.mvt.id!, dto) : this.srv.create(dto);
    obs.subscribe(res => this.ref.close(res));
  }
}
