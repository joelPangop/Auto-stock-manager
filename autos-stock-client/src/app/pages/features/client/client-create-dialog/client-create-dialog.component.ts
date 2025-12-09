import {Component, Inject, OnInit} from '@angular/core';
import {ClientService} from "../../../../services/client.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ModeleDialogData} from "../../../../models/modeleDialogData";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-client-create-dialog',
  templateUrl: './client-create-dialog.component.html',
  styleUrls: ['./client-create-dialog.component.scss']
})
export class ClientCreateDialogComponent implements OnInit {

  form: FormGroup;

  constructor(private clientService: ClientService,
              @Inject(MAT_DIALOG_DATA) public data: ModeleDialogData,
              private ref: MatDialogRef<ClientCreateDialogComponent>,
              private fb: FormBuilder) { }

  ngOnInit(): void {
    this.  form = this.fb.group({
      nom: ['', Validators.required],
      email: ['', Validators.required],
      adresse: ['', Validators.required],
      telephone: ['', Validators.required]
    });
  }

  save(): void {
    if (this.form.invalid) return;
    this.clientService.create(this.form.getRawValue()).subscribe(created => {
      this.ref.close(created); // ← on renvoie le client créé
    });
  }

}
