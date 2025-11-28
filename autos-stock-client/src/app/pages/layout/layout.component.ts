import { Component, ChangeDetectionStrategy } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { map, shareReplay } from 'rxjs/operators';
import { Router } from '@angular/router';
import {AuthService} from "../../services/auth.service";

type NavItem = { icon: string; label: string; path: string; };

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LayoutComponent {
  constructor(
    private bp: BreakpointObserver,
    private auth: AuthService,
    private router: Router
  ) {}

  isHandset$ = this.bp.observe([Breakpoints.Handset, Breakpoints.Tablet])
    .pipe(map(r => r.matches), shareReplay(1));

  nav: NavItem[] = [
    { icon: 'dashboard', label: 'Tableau de bord', path: '/dashboard' },
    // { icon: 'directions_car', label: 'Voitures', path: '/voitures' },
    // { icon: 'receipt_long', label: 'Ventes', path: '/ventes' },
  ];

  go(item: NavItem) { this.router.navigateByUrl(item.path); }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
