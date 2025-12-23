import {Injectable} from '@angular/core';
import {jwtDecode} from 'jwt-decode';
import {User} from "../models/User";

const ACCESS = 'access_token';
const REFRESH = 'refresh_token';
const USER = 'user_token';

@Injectable({providedIn: 'root'})
export class TokenStorageService {
  save(access?: string, refresh?: string, user?: User) {
    localStorage.setItem(ACCESS, access);
    localStorage.setItem(USER, JSON.stringify(user));
    if (refresh) localStorage.setItem(REFRESH, refresh);
  }

  clear() {
    localStorage.removeItem(ACCESS);
    localStorage.removeItem(REFRESH);
    localStorage.removeItem(USER);
  }

  get user(): User {
    return JSON.parse(localStorage.getItem(USER));
  }

  get access(): string | null {
    return localStorage.getItem(ACCESS);
  }

  get refresh(): string | null {
    return localStorage.getItem(REFRESH);
  }

  isExpired(token: string | null): boolean {
    if (!token) return true;
    try {
      const dec: any = jwtDecode(token);
      if (!dec?.exp) return false;
      const now = Math.floor(Date.now() / 1000);
      return dec.exp < now;
    } catch {
      return true;
    }
  }
}
