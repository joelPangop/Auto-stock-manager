import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Fournisseur} from "../models/fournisseur";

@Injectable({
  providedIn: 'root'
})
export class FournisseurService {

  private base = `${environment.apiUrl}/fournisseurs`;

  constructor(private http: HttpClient) {
  }

  getById(id: number):Observable<Fournisseur> {
    return this.http.get<Fournisseur>(`${this.base}/${id}`);
  }

  listAll():Observable<Fournisseur[]> {
    return this.http.get<Fournisseur[]>( `${this.base}`);
  }

}
