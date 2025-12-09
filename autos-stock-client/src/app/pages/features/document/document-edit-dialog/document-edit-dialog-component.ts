import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {DocumentService} from "../../../../services/document.service";
import {Document} from "../../../../models/document";

@Component({
  selector: 'app-document-edit-dialog',
  templateUrl: './document-edit-dialog-component.html',
  styleUrls: ['./document-edit-dialog-component.scss']
})
export class DocumentEditDialogComponent implements OnInit {

  form: FormGroup;
  ngOnInit(): void {
    this.form = this.fb.group({
      type: [''],
      description: ['']
    });
  }

  saving = false;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { doc: Document },
    private fb: FormBuilder,
    private docs: DocumentService,
    private dialogRef: MatDialogRef<DocumentEditDialogComponent>
  ) {
    this.form = this.fb.group({
      type: [data.doc.type, Validators.required],
      description: [data.doc.description || '']
    });
  }

  save() {
    if (this.form.invalid) return;
    this.saving = true;
    this.docs.updateMeta(this.data.doc.id, this.form.value).subscribe({
      next: (updated) => this.dialogRef.close(updated),
      error: () => this.saving = false
    });
  }
}
