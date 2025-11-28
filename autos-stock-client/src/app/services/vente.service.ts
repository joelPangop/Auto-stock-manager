import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Page} from '../models/page.model';
import {environment} from "../../environments/environment";
import {Vente} from "../models/vente";
import {VenteCreateDto, VenteDto} from "../models/VenteCreateDto";

@Injectable({providedIn: 'root'})
export class VenteService {
  private base = `${environment.apiUrl}/ventes`;

  constructor(private http: HttpClient) {
  }

  getPage(page = 0, size = 10, sort = 'id,desc', q?: string): Observable<Page<Vente>> {
    let params = new HttpParams().set('page', String(page)).set('size', String(size)).set('sort', sort);
    if (q) params = params.set('q', q);
    return this.http.get<Page<Vente>>(this.base, {params});
  }

  getById(id: number) {
    return this.http.get<Vente>(`${this.base}/${id}`);
  }

  create(dto: VenteCreateDto): Observable<VenteDto> {
    return this.http.post<VenteDto>(this.base, dto);
  }

  update(id: number, v: Partial<Vente>) {
    return this.http.put<Vente>(`${this.base}/${id}`, v);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
