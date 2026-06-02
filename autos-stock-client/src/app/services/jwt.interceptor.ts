import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, from, defer  } from 'rxjs';
import {  catchError, switchMap } from 'rxjs/operators';
import { TokenStorageService } from './token-storage.service';
import { AuthService } from './auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private tokens: TokenStorageService, private auth: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.tokens.access;
    const isAuthEndpoint = /\/auth\/(login|refresh)/.test(req.url);
    let cloned = req;

    if (token && !isAuthEndpoint) {
      cloned = req.clone({ setHeaders: { Authorization: token } });
    }

    return next.handle(cloned).pipe(
      catchError((err: HttpErrorResponse) => {

        let message = 'Erreur inconnue';

        if (err.error instanceof Blob) {
          // cas backend qui renvoie un Blob (Spring Boot fréquent)
          return from(err.error.text()).pipe(
            switchMap(text => {
              try {
                const json: any = JSON.parse(text);
                message = json.message ?? message;
              } catch {
                message = text;
              }
              return throwError(() => new Error(message));
            })
          );
        }

        if (err.error?.message) {
          message = err.error.message;
        }

        if (err.status === 401 && !isAuthEndpoint) {
          return this.auth.refresh().pipe(
            switchMap(r => {
              const refreshedReq = req.clone({
                setHeaders: {
                  Authorization: `${r.tokenType ?? 'Bearer'} ${r.accessToken}`
                }
              });
              return next.handle(refreshedReq);
            }),
            catchError(e => {
              this.auth.logout();
              return throwError(() => e);
            })
          );
        }

        return throwError(() =>
          new HttpErrorResponse({
            status: err.status,
            statusText: err.statusText,
            url: err.url ?? undefined,
            error: { message }
          }));
      })
    );
  }
}
