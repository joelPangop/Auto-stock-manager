import {Component, Inject, OnInit} from '@angular/core';
import {Observable, of} from "rxjs";
import {Paiement} from "../../../models/paiement";
import {catchError, shareReplay} from "rxjs/operators";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {PaiementService} from "../../../services/paiement.service";

@Component({
  selector: 'app-paiement-view-dialog',
  templateUrl: './paiement-view-dialog.component.html',
  styleUrls: ['./paiement-view-dialog.component.scss']
})
export class PaiementViewDialogComponent implements OnInit {

  errorMsg = '';

  ngOnInit(): void {

  }

  readonly paiement$: Observable<Paiement | null> = this.paiementSrv
    .getById(this.data.paiementId)
    .pipe(
      catchError(err => {
        console.error('Erreur chargement paiement', err);
        this.errorMsg = 'Impossible de charger ce paiement.';
        return of(null);
      }),
      shareReplay({ bufferSize: 1, refCount: true })
    );

  constructor(
    private readonly dialogRef: MatDialogRef<PaiementViewDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public readonly data: any,
    private readonly paiementSrv: PaiementService
  ) {}

  close(): void {
    this.dialogRef.close(false);
  }

  money(v?: number | null): string {
    if (v == null) return '—';
    return `${v.toFixed(2)} $`;
  }
}
