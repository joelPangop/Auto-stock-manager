import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Page} from '../models/page.model';
import {environment} from "../../environments/environment";
import {Document} from "../models/document";

@Injectable({providedIn: 'root'})
export class DocumentService {
  private base = `${environment.apiUrl}/documents`;

  constructor(private http: HttpClient) {
  }

  listByVoiture(voitureId: number) {
    return this.http.get<Document[]>(`${this.base}/voiture/${voitureId}`);
  }

  getPage(page = 0, size = 10, sort = 'id,desc', q?: string): Observable<Page<Document>> {
    let params = new HttpParams().set('page', String(page)).set('size', String(size)).set('sort', sort);
    if (q) params = params.set('q', q);
    return this.http.get<Page<Document>>(this.base, {params});
  }

  upload(voitureId: number, file: File, type: string, description?: string) {
    const fd = new FormData();
    fd.append('file', file);
    fd.append('type', type);
    if (description) fd.append('description', description);
    return this.http.post<Document>(`${this.base}/voiture/${voitureId}`, fd);
  }

  download(id: number) {
    return this.http.get(`${this.base}/${id}/download`, {responseType: 'blob'});
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
