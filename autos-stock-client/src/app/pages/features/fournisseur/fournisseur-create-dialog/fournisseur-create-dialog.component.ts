import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {FournisseurService} from "../../../../services/fournisseur.service";
import {ModeleDialogData} from "../../../../models/modeleDialogData";

@Component({
  selector: 'app-fournisseur-create-dialog',
  templateUrl: './fournisseur-create-dialog.component.html',
  styleUrls: ['./fournisseur-create-dialog.component.scss']
})
export class FournisseurCreateDialogComponent implements OnInit {
  form: FormGroup;

  ngOnInit(): void {
    this.  form = this.fb.group({
      nom: ['', Validators.required],
      type: ['CONCESSION', Validators.required],
      adresse: ['', Validators.required],
      telephone: ['', Validators.required],
    });
  }

  constructor(
    private fb: FormBuilder,
    private srv: FournisseurService,
    private ref: MatDialogRef<FournisseurCreateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ModeleDialogData
  ) {
  }

  save(): void {
    if (this.form.invalid) return;
    this.srv.create(this.form.getRawValue()).subscribe(created => {
      this.ref.close(created); // ← on renvoie le fournisseur créé
    });
  }
}
