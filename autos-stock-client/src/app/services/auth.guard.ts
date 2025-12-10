import {Injectable} from '@angular/core';
import {CanActivate, Router, UrlTree} from '@angular/router';
import {TokenStorageService} from './token-storage.service';

// @ts-ignore
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private tokens: TokenStorageService, private router: Router) {}

  canActivate(): boolean | UrlTree {
    return this.tokens.isExpired(this.tokens.access)
      ? this.router.parseUrl('/login')
      : true;
  }
}
