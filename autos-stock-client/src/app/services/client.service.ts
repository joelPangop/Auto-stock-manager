import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Page} from '../models/page.model';
import {environment} from "../../environments/environment";
import {Client} from "../models/client";

@Injectable({providedIn: 'root'})
export class ClientService {
  private base = `${environment.apiUrl}/clients`;

  constructor(private http: HttpClient) {
  }

  getPage(page = 0, size = 10, sort = 'id,desc', q?: string): Observable<Client[]> {
    let params = new HttpParams().set('page', String(page)).set('size', String(size)).set('sort', sort);
    if (q) params = params.set('q', q);
    return this.http.get<Client[]>(this.base, {params});
  }

  list() { return this.http.get<Client[]>(`${this.base}`); }

  getById(id: number) {
    return this.http.get<Client>(`${this.base}/${id}`);
  }

  create(c: Partial<Client>) {
    return this.http.post<Client>(this.base, c);
  }

  update(id: number, c: Partial<Client>) {
    return this.http.put<Client>(`${this.base}/${id}`, c);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
