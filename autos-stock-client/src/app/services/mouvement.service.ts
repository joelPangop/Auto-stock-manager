import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Page} from '../models/page.model';
import {Mouvement} from "../models/mouvement";
import {environment} from "../../environments/environment";

@Injectable({providedIn: 'root'})
export class MouvementService {
  private base = `${environment.apiUrl}/mouvements`;

  constructor(private http: HttpClient) {
  }

  listByVoiture(voitureId: number) {
    return this.http.get<Mouvement[]>( `http://localhost:8080/api/mouvements/voiture/${voitureId}`);
  }

  getPage(page = 0, size = 10, sort = 'dateMouvement,desc'): Observable<Page<Mouvement>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size)).set('sort', sort);
    return this.http.get<Page<Mouvement>>(this.base, {params});
  }

  create(m: Partial<Mouvement>) {
    return this.http.post<Mouvement>(this.base, m);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  update(id: number, dto: Mouvement)  { return this.http.put<Mouvement>(`${this.base}/${id}`, dto); }
}
