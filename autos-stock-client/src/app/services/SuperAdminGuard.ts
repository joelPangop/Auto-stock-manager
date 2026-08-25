import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Reserve une route au SUPER_ADMIN. Distinct d'AdminGuard, qui laisse aussi
 * passer les ADMIN.
 *
 * Masquer une entree de menu n'est pas une protection : la vraie barriere est
 * cote serveur (@PreAuthorize + regle sur /api/admin/**). Ce garde evite
 * seulement d'afficher un ecran inutilisable.
 */
@Injectable({ providedIn: 'root' })
export class SuperAdminGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate() {
    const allowed = this.auth.isSuperAdmin();
    if (!allowed) this.router.navigate(['/']);
    return allowed;
  }
}
