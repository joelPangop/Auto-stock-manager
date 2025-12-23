import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {User} from "../models/User";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private base = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) { }
  list() { return this.http.get<User[]>(`${this.base}`); }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  update(user): Observable<User> {
    return this.http.put<User>(`${this.base}/${user.id}`, user);
  }

  get(id): Observable<User> {
    return this.http.get<User>(`${this.base}/${id}`);
  }
}
