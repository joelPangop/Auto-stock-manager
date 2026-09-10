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
  isSuperAdmin = false;

  get currentLang(): string { return this.langSvc.current; }

  /**
   * Premier mot du nom de l'utilisateur connecte : le modele n'a pas de champ
   * prenom distinct. Lu via currentUser, dont la valeur n'est re-analysee que
   * si le stockage local change : l'appel depuis le template reste leger et le
   * prenom suit une connexion ou une modification du profil.
   */
  get prenom(): string {
    const nom = this.auth.currentUser?.nom?.trim();
    return nom ? nom.split(/\s+/)[0] : '';
  }

  constructor(
    private bp: BreakpointObserver,
    private auth: AuthService,
    private dialog: MatDialog,
    private router: Router,
    private langSvc: LanguageService
  ) {
    this.isAdmin = this.auth.isAdmin();
    this.isSuperAdmin = this.auth.isSuperAdmin();
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
