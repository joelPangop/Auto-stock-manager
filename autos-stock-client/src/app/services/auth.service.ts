import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import {TokenStorageService} from './token-storage.service';
import {environment} from "../../environments/environment";
import {AuthResponse, LoginRequest, LoginResponse} from "../models/auth/models";
import {tap} from "rxjs/operators";
import {User} from "../models/User";

@Injectable({providedIn: 'root'})
export class AuthService {
  private readonly base = `${environment.apiUrl}/auth`;
  private currentUser$ = new BehaviorSubject<LoginResponse['user'] | null>(null);
  private _onlyMine: boolean = false;
  private cachedUserRaw: string | null | undefined = undefined;

  constructor(private http: HttpClient, private tokens: TokenStorageService) {
  }

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/login`, payload).pipe(
      tap(res => {
        const bearer = res.tokenType ?? 'Bearer';
        this.tokens.save(`${bearer} ${res.accessToken}`, res.refreshToken, res.user);
        this.currentUser$.next(res.user ?? null);
      })
    );
  }

  logout() {
    this.tokens.clear();
    this.currentUser$.next(null);
  }

  isAdmin() {
    return this.currentUser?.role === 'ADMIN' || this.currentUser?.role === 'SUPER_ADMIN';
  }

  isSuperAdmin() {
    return this.currentUser?.role === 'SUPER_ADMIN';
  }

  register(data: { nom: string; email: string; password: string; phoneNumber?: string }) {
    return this.http.post<AuthResponse>(`${this.base}/register`, data).pipe(
      tap(res => {
        // localStorage.setItem('access_token', res.accessToken);
        // localStorage.setItem('refresh_token', res.refreshToken);
        // this.currentUser$.next(res.user);
      })
    );
  }

  me(): Observable<any> {
    return this.http.get<any>(`${this.base}/me`).pipe(
      tap(user => {
        this.currentUser$.next(user)
      })
    );
  }

  get currentUser() {
    const raw = localStorage.getItem('user_token');

    // Ce getter est appele depuis les templates (isAdmin/isSuperAdmin) donc a
    // chaque detection de changement : on ne re-parse et on ne re-emet que si
    // la valeur stockee a reellement change, sinon on boucle.
    if (raw === this.cachedUserRaw) return this.currentUser$.value;
    this.cachedUserRaw = raw;

    let parsed: LoginResponse['user'] | null = null;
    try {
      parsed = raw ? JSON.parse(raw) : null;
    } catch {
      parsed = null;
    }
    this.currentUser$.next(parsed);
    return parsed;
  }

  set currentUser(value: User) {
    this.currentUser$.next(value);
    this.tokens.save(this.tokens.access, this.tokens.refresh, value);
  }

  refresh(): Observable<{ accessToken: string; tokenType?: string; expiresIn?: number; }> {
    return this.http.post<any>(`${this.base}/refresh`, {refreshToken: this.tokens.refresh}).pipe(
      tap(res => {
        const bearer = res.tokenType ?? 'Bearer';
        this.tokens.save(`${bearer} ${res.accessToken}`, this.tokens.refresh!);
      })
    );
  }

  user$() {
    return this.currentUser$.asObservable();
  }

  isAuthenticated(): boolean {
    return !this.tokens.isExpired(this.tokens.access);
  }

// auth.util.ts
  getUserIdFromToken(): number | null {
    const raw = localStorage.getItem('access_token'); // adapte si autre storage
    if (!raw) return null;
    const payload = JSON.parse(atob(raw.split('.')[1]));
    return payload?.uid ?? null; // adapte à ta claim
  }

  forgotPassword(payload: { identifier: string; deliveryMethod: 'EMAIL' | 'SMS' }) {
    return this.http.post<void>(`${this.base}/forgot-password`, payload);
  }

  resetPassword(payload: { identifier: string; code: string; newPassword: string }) {
    return this.http.post<void>(`${this.base}/reset-password`, payload);
  }

  /** Changement de mot de passe par l'utilisateur connecte (mot de passe actuel exige). */
  changePassword(payload: { currentPassword: string; newPassword: string }) {
    return this.http.post<void>(`${this.base}/change-password`, payload);
  }

  get onlyMine(): boolean {
    return this._onlyMine;
  }

  set onlyMine(value: boolean) {
    this._onlyMine = value;
  }
}
