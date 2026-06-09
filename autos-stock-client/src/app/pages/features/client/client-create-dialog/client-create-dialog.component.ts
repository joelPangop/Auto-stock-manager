import { Component, Inject, OnInit } from '@angular/core';
import { ClientService } from '../../../../services/client.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Client } from '../../../../models/client';

export interface ClientDialogData {
  client?: Client;  // si fourni → mode édition
}

@Component({
  selector: 'app-client-create-dialog',
  templateUrl: './client-create-dialog.component.html',
  styleUrls: ['./client-create-dialog.component.scss']
})
export class ClientCreateDialogComponent implements OnInit {
  form: FormGroup;
  editMode: boolean;

  constructor(
    private clientService: ClientService,
    @Inject(MAT_DIALOG_DATA) public data: ClientDialogData,
    private ref: MatDialogRef<ClientCreateDialogComponent>,
    private fb: FormBuilder
  ) {
    this.editMode = !!(data?.client);
  }

  ngOnInit(): void {
    const c = this.data?.client;
    this.form = this.fb.group({
      nom:       [c?.nom       ?? '', Validators.required],
      email:     [c?.email     ?? '', Validators.required],
      adresse:   [c?.adresse   ?? ''],
      telephone: [c?.telephone ?? ''],
    });
  }

  save(): void {
    if (this.form.invalid) return;
    const val = this.form.getRawValue();
    if (this.editMode && this.data.client?.id) {
      this.clientService.update(this.data.client.id, val).subscribe(updated => {
        this.ref.close(updated);
      });
    } else {
      this.clientService.create(val).subscribe(created => {
        this.ref.close(created);
      });
    }
  }
}
