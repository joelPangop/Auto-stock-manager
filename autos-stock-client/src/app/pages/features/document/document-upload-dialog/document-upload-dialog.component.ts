import {Component, Inject, OnInit} from '@angular/core';
import {DocumentService} from "../../../../services/document.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, Validators} from "@angular/forms";

@Component({
  selector: 'app-document-upload-dialog',
  templateUrl: './document-upload-dialog.component.html',
  styleUrls: ['./document-upload-dialog.component.scss']
})
export class DocumentUploadDialogComponent implements OnInit {

  file?: File;
  form = this.fb.group({
    type: ['', Validators.required],
    description: ['']
  });
  uploading = false;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { idVoiture: number },
    private fb: FormBuilder,
    private docs: DocumentService,
    private dialogRef: MatDialogRef<DocumentUploadDialogComponent>
  ) {}

  ngOnInit(): void {
        // throw new Error("Method not implemented.");
    }

  onFileChange(e: Event) {
    const input = e.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.file = input.files[0];
    }
  }

  save() {
    if (!this.file || this.form.invalid) return;
    this.uploading = true;
    const meta = { type: this.form.value.type, description: this.form.value.description };
    this.docs.upload(this.data.idVoiture, this.file, meta).subscribe((res) => this.dialogRef.close(res));
  }
}
