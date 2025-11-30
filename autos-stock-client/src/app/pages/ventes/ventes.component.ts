import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {VenteService} from "../../services/vente.service";
import {Vente} from "../../models/vente";

@Component({
  selector: 'app-ventes',
  templateUrl: './ventes.component.html',
  styleUrls: ['./ventes.component.scss']
})
export class VentesComponent implements OnInit {
  displayed = ['marque', 'modele', 'nomClient', 'nomVendeur', 'prixFinal', 'modePaiement', 'actions'];
  data = new MatTableDataSource<Vente>([]);
  total = 0;
  pageIndex = 0;
  pageSize = 10;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(private srv: VenteService) {
  }

  ngOnInit() {
    this.reload();
  }

  reload() {
    this.srv.getPage(this.pageIndex, this.pageSize).subscribe(p => {
      console.log("Ventes", p);
      this.data.data = p;
      this.total = p.length;
    });
  }

  onPage(e: any) {
    this.pageIndex = e.pageIndex;
    this.pageSize = e.pageSize;
    this.reload();
  }

  remove(r: Vente) {
    if (confirm('Supprimer cette vente ?')) this.srv.delete(r.id).subscribe(() => this.reload());
  }
}
