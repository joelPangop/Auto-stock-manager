import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { Client } from '../../models/client';
import { ClientService } from '../../services/client.service';
import { AuthService } from '../../services/auth.service';
import { MatDialog } from '@angular/material/dialog';
import { ClientCreateDialogComponent } from '../features/client/client-create-dialog/client-create-dialog.component';

@Component({
  selector: 'app-clients-list',
  templateUrl: './clients.component.html',
  styleUrls: ['./clients.component.scss']
})
export class ClientsComponent implements OnInit {
  isAdmin = false;
  displayed = ['id', 'nom', 'email', 'telephone', 'createdAt', 'actions'];
  data = new MatTableDataSource<Client>([]);
  total = 0;
  pageIndex = 0;
  pageSize = 10;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private srv: ClientService,
    private auth: AuthService,
    private dialog: MatDialog
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

  edit(c: Client) {
    const ref = this.dialog.open(ClientCreateDialogComponent, {
      width: '420px',
      disableClose: true,
      data: { client: c }
    });
    ref.afterClosed().subscribe(updated => {
      if (updated) this.reload();
    });
  }

  remove(r: Client) {
    if (confirm('Supprimer ?')) this.srv.delete(r.id).subscribe(() => this.reload());
  }
}
