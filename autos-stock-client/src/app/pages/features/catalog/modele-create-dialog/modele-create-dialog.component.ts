import {Component, Inject, OnInit} from '@angular/core';
import {ModeleService} from "../../../../services/modele.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ModeleDialogData} from "../../../../models/modeleDialogData";
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {Modele} from "../../../../models/modele";

@Component({
  selector: 'app-modele-create-dialog',
  templateUrl: './modele-create-dialog.component.html',
  styles: [`.full{width:100%}`]
})
export class ModeleCreateDialogComponent implements OnInit {
  loading = false;
  form: FormGroup;
  constructor(
    private fb: FormBuilder,
    private srv: ModeleService,
    public ref: MatDialogRef<ModeleCreateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ModeleDialogData
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({ nom: ['', Validators.required] });
  }
  save() {
    this.loading = true;
    this.srv.create(this.form.value.nom!, this.data.idMarque).subscribe({
      next: (m: Modele) => { this.loading = false; this.ref.close(m); },
      error: _ => this.loading = false
    });
  }
}
