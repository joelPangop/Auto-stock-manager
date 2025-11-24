import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
// @ts-ignore
import { Observable, catchError, switchMap, throwError } from 'rxjs';
import { TokenStorageService } from './token-storage.service';
import { AuthService } from './auth.service';

// @ts-ignore
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
      catchError((err: any) => {
        if (err instanceof HttpErrorResponse && err.status === 401 && !isAuthEndpoint) {
          // tentative de refresh
          return this.auth.refresh().pipe(
            switchMap(r => {
              const refreshedReq = req.clone({
                setHeaders: { Authorization: `${r.tokenType ?? 'Bearer'} ${r.accessToken}` }
              });
              return next.handle(refreshedReq);
            }),
            catchError(e => {
              this.auth.logout();
              return throwError(() => e);
            })
          );
        }
        return throwError(() => err);
      })
    );
  }
}
