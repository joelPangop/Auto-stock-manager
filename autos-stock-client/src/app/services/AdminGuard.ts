// admin.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}
  canActivate() {
    const allowed = this.auth.isAdmin();
    if (!allowed) this.router.navigate(['/']);
    return allowed;
  }
}
