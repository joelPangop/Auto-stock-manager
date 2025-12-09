import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Page} from '../models/page.model';
import {environment} from "../../environments/environment";
import {Document} from "../models/document";
import {DocumentCreateMeta} from "../models/DocumentCreateMeta";
import {DocumentUpdateMeta} from "../models/DocumentUpdateMeta";

@Injectable({providedIn: 'root'})
export class DocumentService {
  private base = `${environment.apiUrl}/documents`;

  constructor(private http: HttpClient) {
  }

  listByVoiture(voitureId: number): Observable<Document[]>{
    return this.http.get<Document[]>(`${this.base}/${voitureId}`);
  }

  getPage(page = 0, size = 10, sort = 'id,desc', q?: string): Observable<Page<Document>> {
    let params = new HttpParams().set('page', String(page)).set('size', String(size)).set('sort', sort);
    if (q) params = params.set('q', q);
    return this.http.get<Page<Document>>(this.base, {params});
  }

  upload(voitureId: number, file: File, meta: DocumentCreateMeta): Observable<Document> {
    const fd = new FormData();
    fd.append('file', file, file.name);
    // envoyer l’objet JSON comme Blob pour rester en multipart
    fd.append(
      'meta',
      new Blob([JSON.stringify(meta)], { type: 'application/json' })
    );

    return this.http.post<Document>(`${this.base}/voiture/${voitureId}`, fd);
  }

  updateMeta(documentId: number, patch: DocumentUpdateMeta): Observable<Document> {
    return this.http.patch<Document>(`${this.base}/${documentId}`, patch);
  }

  download(documentId: number): Observable<Blob> {
    return this.http.get(`${this.base}/${documentId}/download`, {
      responseType: 'blob'
    });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
