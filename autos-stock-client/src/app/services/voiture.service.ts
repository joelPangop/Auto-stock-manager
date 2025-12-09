import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Page} from '../models/page.model';
import {environment} from "../../environments/environment";
import {VoitureDetailDto} from "../models/VoitureDetailDto";
import {StatutVoiture} from "../models/enums/StatutVoiture";
import {VoitureListDto} from "../models/VoitureListDto";
import {VoitureCreateDto} from "../models/VoitureCreateDto";

@Injectable({ providedIn: 'root' })
export class VoitureService {
  private base = `${environment.apiUrl}/voitures`;

  constructor(private http: HttpClient) {}

  /**
   * GET /voitures?marque=&statut=
   * -> renvoie List<VoitureListDto> (selon ton contrôleur)
   */
  list(marque?: string, statut?: StatutVoiture): Observable<VoitureListDto[]> {
    let params = new HttpParams();
    if (marque && marque.trim().length) params = params.set('marque', marque.trim());
    if (statut) params = params.set('statut', statut);
    return this.http.get<VoitureListDto[]>(this.base, { params });
  }

  getMine(marque?: string, statut?: StatutVoiture) {
    let params = new HttpParams();
    if (marque && marque.trim().length) params = params.set('marque', marque.trim());
    if (statut) params = params.set('statut', statut);
    return this.http.get<VoitureListDto[]>(`${this.base}/mine`, { params });
  }

  /**
   * GET /voitures/{id} -> détail (si exposé côté back)
   */
  getById(id: number): Observable<VoitureDetailDto> {
    const response: Observable<VoitureDetailDto> = this.http.get<VoitureDetailDto>(`${this.base}/${id}`);
    console.log(response);
    return response;
  }

  /**
   * POST /voitures, PUT /voitures/{id}, DELETE /voitures/{id}
   * (à adapter à tes DTO de commande si tu en as)
   */
  create(payload: VoitureCreateDto) {
    return this.http.post<VoitureDetailDto>(this.base, payload);
  }

  update(id: number, payload: Partial<VoitureDetailDto>) {
    return this.http.put<VoitureDetailDto>(`${this.base}/${id}`, payload);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /** Exemple si tu as /voitures/count */
  count() { return this.http.get<number>(`${this.base}/count`); }
}
