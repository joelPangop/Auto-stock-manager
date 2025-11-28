import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Page} from '../models/page.model';
import {Paiement} from "../models/paiement";
import {environment} from "../../environments/environment";

@Injectable({providedIn: 'root'})
export class PaiementService {
  private base = `${environment.apiUrl}/paiements`;

  constructor(private http: HttpClient) {
  }

  getPage(page = 0, size = 10, sort = 'datePaiement,desc'): Observable<Page<Paiement>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size)).set('sort', sort);
    return this.http.get<Page<Paiement>>(this.base, {params});
  }

  listByVente(venteId: number) {
    return this.http.get<Paiement[]>(`${this.base}/vente/${venteId}`);
  }

  create(p: Partial<Paiement>) {
    return this.http.post<Paiement>(this.base, p);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
