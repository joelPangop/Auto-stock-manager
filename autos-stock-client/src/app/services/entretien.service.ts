import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Page} from '../models/page.model';
import {Entretien} from "../models/entretien";
import {environment} from "../../environments/environment";

@Injectable({providedIn: 'root'})
export class EntretienService {
  private base = `${environment.apiUrl}/entretiens`;

  constructor(private http: HttpClient) {
  }

  listByVoiture(voitureId: number) {
    return this.http.get<Entretien[]>(`${this.base}/voiture/${voitureId}`);
  }

  getPage(page = 0, size = 10, sort = 'dateEntretien,desc'): Observable<Page<Entretien>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size)).set('sort', sort);
    return this.http.get<Page<Entretien>>(this.base, {params});
  }

  create(e: Partial<Entretien>) {
    return this.http.post<Entretien>(this.base, e);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
