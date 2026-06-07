import {Component, Inject, OnInit} from '@angular/core';
import {Entretien} from "../../../../models/entretien";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {EntretienService} from "../../../../services/entretien.service";

@Component({
  selector: 'app-entretien-view-dialog',
  templateUrl: './entretien-view-dialog.component.html',
  styleUrls: ['./entretien-view-dialog.component.scss']
})
export class EntretienViewDialogComponent implements OnInit {

  readonly entretien$ = this.data.entretien;

  errorMsg = '';

  ngOnInit() {
  }

  constructor(
    private readonly dialogRef: MatDialogRef<EntretienViewDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { entretien: Entretien },
    private readonly entretienSrv: EntretienService
  ) {}

  close(): void {
    this.dialogRef.close(false);
  }

  formatMoney(v?: number | null): string {
    if (v == null) return '—';
    // simple, sans Intl pour rester compatible partout
    return `${v.toFixed(2)} $`;
  }

}
