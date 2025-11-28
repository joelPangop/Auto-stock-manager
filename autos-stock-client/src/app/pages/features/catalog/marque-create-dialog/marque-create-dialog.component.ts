import { Component, OnInit } from '@angular/core';
import {MarqueService} from "../../../../services/marque.service";
import {Marque} from "../../../../models/marque";
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-marque-create-dialog',
  templateUrl: './marque-create-dialog.component.html',
  styles: [`.full{width:100%}`]
})
export class MarqueCreateDialogComponent implements OnInit {
  loading = false;
  form: FormGroup;
  constructor(private fb: FormBuilder, private srv: MarqueService, public ref: MatDialogRef<MarqueCreateDialogComponent>) {}

  ngOnInit(): void {
    this.form = this.fb.group({ nom: ['', Validators.required] });
  }

  save() {
    this.loading = true;
    this.srv.create(this.form.value.nom!).subscribe({
      next: (m: Marque) => { this.loading = false; this.ref.close(m); },
      error: _ => this.loading = false
    });
  }

}
