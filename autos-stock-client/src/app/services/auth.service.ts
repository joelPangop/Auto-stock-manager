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
    return this.currentUser?.role === 'ADMIN';
  }

  register(data: { nom: string; email: string; password: string }) {
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
    const user = localStorage.getItem('user_token');
    this.currentUser$.next(JSON.parse(user));
    return this.currentUser$.value;
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

  get onlyMine(): boolean {
    return this._onlyMine;
  }

  set onlyMine(value: boolean) {
    this._onlyMine = value;
  }
}
