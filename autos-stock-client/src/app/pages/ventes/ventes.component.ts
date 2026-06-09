import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { VenteService } from '../../services/vente.service';
import { Vente } from '../../models/vente';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../services/auth.service';
import { VenteEditDialogComponent } from '../features/ventes/vente-edit-dialog/vente-edit-dialog.component';

@Component({
  selector: 'app-ventes',
  templateUrl: './ventes.component.html',
  styleUrls: ['./ventes.component.scss']
})
export class VentesComponent implements OnInit {
  isAdmin = false;
  get displayed(): string[] {
    const cols = ['marque', 'modele', 'nomClient', 'nomVendeur', 'prixFinal', 'modePaiement', 'actions'];
    return cols;
  }
  data = new MatTableDataSource<Vente>([]);
  total = 0;
  pageIndex = 0;
  pageSize = 10;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private srv: VenteService,
    private dialog: MatDialog,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.isAdmin = this.auth.isAdmin();
    this.reload();
  }

  reload() {
    this.srv.getPage(this.pageIndex, this.pageSize).subscribe(p => {
      this.data.data = p;
      this.total = p.length;
    });
  }

  onPage(e: any) {
    this.pageIndex = e.pageIndex;
    this.pageSize = e.pageSize;
    this.reload();
  }

  edit(r: Vente) {
    const ref = this.dialog.open(VenteEditDialogComponent, {
      width: '520px',
      disableClose: true,
      data: r
    });
    ref.afterClosed().subscribe(updated => {
      if (updated) this.reload();
    });
  }

  remove(r: Vente) {
    if (confirm('Supprimer cette vente ?')) this.srv.delete(r.id).subscribe(() => this.reload());
  }
}
