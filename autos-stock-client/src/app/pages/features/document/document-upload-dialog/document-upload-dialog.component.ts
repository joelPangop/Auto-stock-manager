import { Component, Inject, OnInit } from '@angular/core';
import { DocumentService } from '../../../../services/document.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';

export interface FilePreview {
  file: File;
  previewUrl: string | null;
}

@Component({
  selector: 'app-document-upload-dialog',
  templateUrl: './document-upload-dialog.component.html',
  styleUrls: ['./document-upload-dialog.component.scss']
})
export class DocumentUploadDialogComponent implements OnInit {

  filePreviews: FilePreview[] = [];
  uploading = false;
  uploadProgress = 0;   // 0-100

  form = this.fb.group({
    type: ['', Validators.required],
    description: [''],
    montant: ['']
  });

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { idVoiture: number; defaultType?: string },
    private fb: FormBuilder,
    private docs: DocumentService,
    private dialogRef: MatDialogRef<DocumentUploadDialogComponent>
  ) {}

  ngOnInit(): void {
    if (this.data.defaultType) {
      this.form.patchValue({ type: this.data.defaultType });
    }
  }

  get isPhotoType(): boolean {
    return this.form.get('type')?.value === 'PHOTO';
  }

  onFileChange(e: Event) {
    const input = e.target as HTMLInputElement;
    if (!input.files || !input.files.length) return;

    const newFiles = Array.from(input.files);
    const existing = new Set(this.filePreviews.map(fp => fp.file.name + fp.file.size));

    newFiles.forEach(file => {
      const key = file.name + file.size;
      if (existing.has(key)) return; // éviter les doublons

      const isImage = file.type.startsWith('image/');
      const preview: FilePreview = { file, previewUrl: null };

      if (isImage) {
        const reader = new FileReader();
        reader.onloadend = () => { preview.previewUrl = reader.result as string; };
        reader.readAsDataURL(file);
      }

      this.filePreviews.push(preview);
    });

    // Reset l'input pour permettre de re-sélectionner les mêmes fichiers si besoin
    input.value = '';
  }

  removeFile(index: number) {
    this.filePreviews.splice(index, 1);
  }

  save() {
    if (!this.filePreviews.length || this.form.invalid) return;
    this.uploading = true;
    this.uploadProgress = 0;

    const meta = {
      type: this.form.value.type,
      description: this.form.value.description
    };

    const total = this.filePreviews.length;
    let done = 0;

    const uploads$ = this.filePreviews.map(fp =>
      new Promise<void>((resolve) => {
        this.docs.upload(this.data.idVoiture, fp.file, meta).subscribe({
          next: () => {
            done++;
            this.uploadProgress = Math.round((done / total) * 100);
            resolve();
          },
          error: () => {
            done++;
            this.uploadProgress = Math.round((done / total) * 100);
            resolve(); // on continue même si un fichier échoue
          }
        });
      })
    );

    Promise.all(uploads$).then(() => {
      this.uploading = false;
      this.dialogRef.close(true);
    });
  }
}
