import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import {Marque} from "../models/marque";

@Injectable({ providedIn: 'root' })
export class MarqueService {
  private base = `${environment.apiUrl}/marques`;
  constructor(private http: HttpClient) {}
  list(): Observable<Marque[]> { return this.http.get<Marque[]>(this.base); }
  create(nom: string): Observable<Marque> { return this.http.post<Marque>(this.base, { nom }); }
}
