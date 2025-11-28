import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";

@Injectable({providedIn: 'root'})
export class StatsService {
  private base = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {
  }

  voituresCount(): Observable<number>{
    return this.http.get<number>(`${this.base}/voitures/count`);
  }

  ventesCount() {
    return this.http.get<number>(`${this.base}/ventes/count`);
  }

  clientsCount() {
    return this.http.get<number>(`${this.base}/clients/count`);
  }
}
