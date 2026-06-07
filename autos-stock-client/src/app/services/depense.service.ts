import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {DepenseCreateDto} from "../models/DepenseCreateDto";
import {Observable} from "rxjs";
import {DepenseDto} from "../models/DepenseDto";
import {DepenseMonthlyTotalDto} from "../models/DepenseMonthlyTotalDto";
import {PageVm} from "../models/PageVm";
import {DepenseDashboardVm} from "../models/DepenseDashboardVm";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class DepenseService {

  private base = `${environment.apiUrl}/depenses`;
  constructor(private http: HttpClient) {}

  create(voitureId: number, dto: DepenseCreateDto): Observable<DepenseDto> {
    return this.http.post<DepenseDto>(`${this.base}/${voitureId}`, dto);
  }

  update(voitureId: number, depenseId: number, dto: DepenseCreateDto): Observable<DepenseDto> {
    return this.http.put<DepenseDto>(`${this.base}/${voitureId}/${depenseId}`, dto);
  }

  monthly(voitureId: number, start: string, end: string): Observable<DepenseMonthlyTotalDto[]> {
    const params = new HttpParams().set('start', start).set('end', end);
    return this.http.get<DepenseMonthlyTotalDto[]>(`${this.base}/${voitureId}/monthly`, { params });
  }

  list(voitureId: number, page = 0, size = 10): Observable<PageVm<DepenseDto>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<PageVm<DepenseDto>>(`${this.base}/${voitureId}`, { params });
  }

  listNonJustifiees(voitureId: number, page = 0, size = 10): Observable<PageVm<DepenseDto>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<PageVm<DepenseDto>>(`${this.base}/${voitureId}/non-justifiees`, { params });
  }

  // monthly(voitureId: number, start: string, end: string): Observable<DepenseMonthlyTotalDto[]> {
  //   const params = new HttpParams().set('start', start).set('end', end);
  //   return this.http.get<DepenseMonthlyTotalDto[]>(`/api/voitures/${voitureId}/depenses/monthly`, { params });
  // }

  dashboard(voitureId: number, page = 0, size = 10, start?: string, end?: string): Observable<DepenseDashboardVm> {
    let params = new HttpParams().set('page', String(page)).set('size', String(size))
      .set('includeMonthly', 'true')
      .set('includeMonthlyByCategorie', 'false');

    if (start && end) {
      params = params.set('start', start+"T00:00:00").set('end', end+"T00:00:00");
    }
    return this.http.get<DepenseDashboardVm>(`${this.base}/${voitureId}/dashboard`, { params });
  }

  getDepense(voitureId: number, id: number): Observable<DepenseDto> {
    return this.http.get<DepenseDto>(`${this.base}/${voitureId}/${id}`);
  }

  delete(voitureId: number, depenseId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${voitureId}/${depenseId}`);
  }
}
