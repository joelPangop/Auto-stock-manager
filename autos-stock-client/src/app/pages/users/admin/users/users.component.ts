import { Component, OnInit } from '@angular/core';
import {UserService} from '../../../../services/user.service';
import {AuthService} from '../../../../services/auth.service';
import {NavigationEnd, Router} from '@angular/router';
import {Subscription} from 'rxjs';
import {filter} from 'rxjs/operators';
import {MatTableDataSource} from '@angular/material/table';
import {User} from '../../../../models/User';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {

  data = new MatTableDataSource<User>([]);
  loading = false;
  onlyAdmins = false;
  displayedColumns = ['nom', 'email', 'role', 'status', 'actions'];

  total = 0;
  pageSize = 5;
  pageIndex = 0;

  private sub?: Subscription;

  constructor(
    private api: UserService,
    private auth: AuthService,
    private router: Router
  ) {}

  get isSuperAdmin(): boolean {
    return this.auth.isSuperAdmin();
  }

  ngOnInit(): void {
    this.load();
    this.sub = this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe((e: any) => {
        if (e.urlAfterRedirects?.startsWith('/users')) this.load();
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
        this.data.data = this.onlyAdmins
          ? arr.filter(u => u.role === 'ADMIN' || u.role === 'SUPER_ADMIN')
          : arr;
        this.total = Math.ceil(arr.length / this.pageSize);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        alert('Erreur chargement utilisateurs');
      }
    });
  }

  create() { this.router.navigate(['/register']); }

  edit(id: number) { this.router.navigate([`/register/${id}`]); }

  delete(id: number) {
    if (!confirm('Supprimer cet utilisateur ?')) return;
    this.api.delete(id).subscribe({
      next: () => this.load(),
      error: (err) => alert(err?.error?.message ?? 'Suppression impossible')
    });
  }

  regeneratePassword(id: number) {
    if (!confirm('Régénérer un nouveau mot de passe temporaire et l\'envoyer par email ?')) return;
    this.api.regeneratePassword(id).subscribe({
      next: () => {
        this.load();
        alert('Nouveau mot de passe envoyé par email.');
      },
      error: (err) => alert(err?.error?.message ?? 'Erreur lors de la régénération')
    });
  }
}
