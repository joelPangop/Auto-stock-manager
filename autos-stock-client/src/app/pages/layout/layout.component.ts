import { Component } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { map, shareReplay } from 'rxjs/operators';
import { Router } from '@angular/router';
import {AuthService} from "../../services/auth.service";
import {MatDialog} from "@angular/material/dialog";
import {ProfileDialogComponent} from "../features/user/profile-dialog/profile-dialog.component";
import {LanguageService} from "../../services/language.service";

type NavItem = { icon: string; label: string; path: string; };

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],

})
export class LayoutComponent {

  isAdmin = false;

  get currentLang(): string { return this.langSvc.current; }

  constructor(
    private bp: BreakpointObserver,
    private auth: AuthService,
    private dialog: MatDialog,
    private router: Router,
    private langSvc: LanguageService
  ) {
    this.isAdmin = this.auth.isAdmin();
  }

  setLang(lang: string): void { this.langSvc.use(lang); }

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

  openProfileDialog() {
    this.dialog.open(ProfileDialogComponent, {
      width: '420px',
      data: this.auth.currentUser, // optionnel
      autoFocus: false
    });
  }

}
