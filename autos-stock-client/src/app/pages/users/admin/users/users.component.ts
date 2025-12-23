import { Component, OnInit } from '@angular/core';
import {UserService} from "../../../../services/user.service";
import {NavigationEnd, Router} from "@angular/router";
import {Subscription} from "rxjs";
import {filter} from "rxjs/operators";
import {MatTableDataSource} from "@angular/material/table";
import {User} from "../../../../models/User";

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {

  data = new MatTableDataSource<User>([]);
  loading = false;
  onlyAdmins = false;
  displayedColumns = ['nom', 'email', 'role', 'actions'];


  total = 0;
  pageSize = 5;
  pageIndex = 0;

  private sub?: Subscription;

  constructor(private api: UserService, private router: Router) {}

  ngOnInit(): void {
    this.load();

    this.sub = this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe((e: any) => {
        // recharge quand on revient sur /users
        if (e.urlAfterRedirects?.startsWith('/users')) {
          this.load();
        }
      });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  load() {
    this.loading = true;
    this.api.list().subscribe({
      next: (data) => {
        const arr = Array.isArray(data) ? data : [];
        this.data.data = this.onlyAdmins ? arr.filter(u => u.role === 'ADMIN') : arr;
        this.total = Math.ceil(arr.length/this.pageSize);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        alert('Erreur chargement utilisateurs');
      }
    });
  }

  create() {
    this.router.navigate(['/register']); // ta page register existante
  }

  edit(id: number) {
    // Choix 1: route param
    this.router.navigate([`/register/${id}`]);

    // Choix 2: query param si tu préfères :
    // this.router.navigate(['/register'], { queryParams: { id } });
  }

  delete(id: number) {
    if (!confirm('Supprimer cet utilisateur ?')) return;
    this.api.delete(id).subscribe({
      next: () => this.load(),
      error: (err) => alert(err?.error?.message ?? 'Suppression impossible')
    });
  }
}
