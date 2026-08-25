import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';

@Injectable({providedIn: 'root'})
export class BackupService {
  private readonly base = `${environment.apiUrl}/admin/backup`;

  constructor(private http: HttpClient) {}

  /**
   * Telecharge l'archive d'export. La reponse complete est demandee (et non le
   * seul corps) pour lire le nom de fichier dans Content-Disposition.
   */
  export(includeUploads: boolean, includeS3: boolean): Observable<HttpResponse<Blob>> {
    const params = `?includeUploads=${includeUploads}&includeS3=${includeS3}`;
    return this.http.get(`${this.base}/export${params}`, {
      responseType: 'blob',
      observe: 'response'
    });
  }
}
