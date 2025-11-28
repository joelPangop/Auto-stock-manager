import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { TokenStorageService } from './token-storage.service';
import {environment} from "../../environments/environment";
import {AuthResponse, LoginRequest, LoginResponse} from "../models/auth/models";
import {tap} from "rxjs/operators";

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly base = `${environment.apiUrl}/auth`;
  private currentUser$ = new BehaviorSubject<LoginResponse['user'] | null>(null);

  constructor(private http: HttpClient, private tokens: TokenStorageService) {}

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/login`, payload).pipe(
      tap(res => {
        const bearer = res.tokenType ?? 'Bearer';
        this.tokens.save(`${bearer} ${res.accessToken}`, res.refreshToken);
        this.currentUser$.next(res.user ?? null);
      })
    );
  }

  logout() {
    this.tokens.clear();
    this.currentUser$.next(null);
  }

  register(data: { nom: string; email: string; password: string }) {
    return this.http.post<AuthResponse>(`${this.base}/register`, data).pipe(
      tap(res => {
        localStorage.setItem('accessToken', res.accessToken);
        localStorage.setItem('refreshToken', res.refreshToken);
        this.currentUser$.next(res.user);
      })
    );
  }

  me(): Observable<any> {
    return this.http.get<any>(`${this.base}/me`).pipe(
      tap(user => this.currentUser$.next(user))
    );
  }

  refresh(): Observable<{ accessToken: string; tokenType?: string; expiresIn?: number; }> {
    return this.http.post<any>(`${this.base}/refresh`, { refreshToken: this.tokens.refresh }).pipe(
      tap(res => {
        const bearer = res.tokenType ?? 'Bearer';
        this.tokens.save(`${bearer} ${res.accessToken}`, this.tokens.refresh!);
      })
    );
  }

  user$() { return this.currentUser$.asObservable(); }
  isAuthenticated(): boolean { return !this.tokens.isExpired(this.tokens.access); }
}
