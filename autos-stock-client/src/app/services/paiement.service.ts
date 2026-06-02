import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Page} from '../models/page.model';
import {Paiement} from "../models/paiement";
import {environment} from "../../environments/environment";
import {PageVm} from "../models/PageVm";

@Injectable({providedIn: 'root'})
export class PaiementService {
  private base = `${environment.apiUrl}/paiements`;

  constructor(private http: HttpClient) {
  }

  getPage(page = 0, size = 10, sort = 'datePaiement,desc', onlyMine = false) {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', sort)
      .set('onlyMine', String(onlyMine));

    return this.http.get<PageVm<Paiement>>(this.base, { params });
  }

  listByVente(venteId: number) {
    return this.http.get<Paiement[]>(`${this.base}/vente/${venteId}`);
  }

  create(p: Paiement):Observable<Paiement> {
    return this.http.post<Paiement>(`${this.base}/${p.venteId}`, p);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  getById(id: number) {
    return this.http.get<Paiement>(`${this.base}/${id}`);
  }
}
