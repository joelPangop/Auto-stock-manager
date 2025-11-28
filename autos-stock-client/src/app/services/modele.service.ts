import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import {Modele} from "../models/modele";

@Injectable({ providedIn: 'root' })
export class ModeleService {
  private base = `${environment.apiUrl}/modeles`;
  constructor(private http: HttpClient) {}
  listByMarque(idMarque: number): Observable<Modele[]> {
    return this.http.get<Modele[]>(`${this.base}?idMarque=${idMarque}`);
  }
  create(nom: string, idMarque: number): Observable<Modele> {
    return this.http.post<Modele>(this.base, { nom, idMarque });
  }
}
