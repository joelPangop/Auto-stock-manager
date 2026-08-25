import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {UserService} from './user.service';
import {BackupService} from './backup.service';
import {environment} from '../../environments/environment';

/**
 * Services d'administration : gestion des comptes et export des donnees.
 *
 * Le point central est le drapeau emailSent. Auparavant l'API renvoyait un
 * corps vide et l'ecran annoncait un succes meme quand l'invitation n'etait
 * pas partie — le compte existait alors avec un mot de passe temporaire que
 * personne ne connaissait.
 */
describe('UserService', () => {
  let service: UserService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/users`;

  beforeEach(() => {
    TestBed.configureTestingModule({imports: [HttpClientTestingModule]});
    service = TestBed.inject(UserService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('adminCreate poste les donnees du compte', () => {
    service.adminCreate({nom: 'Nouveau', email: 'n@test.fr', role: 'USER'}).subscribe();

    const req = http.expectOne(`${base}/admin-create`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({nom: 'Nouveau', email: 'n@test.fr', role: 'USER'});
    req.flush({emailSent: true});
  });

  it('adminCreate expose emailSent a true quand l invitation est partie', () => {
    let res: any = null;
    service.adminCreate({nom: 'N', email: 'n@test.fr'}).subscribe(r => (res = r));
    http.expectOne(`${base}/admin-create`).flush({emailSent: true});

    expect(res.emailSent).toBe(true);
  });

  it('adminCreate expose emailSent a false quand l email a echoue', () => {
    let res: any = null;
    service.adminCreate({nom: 'N', email: 'n@test.fr'}).subscribe(r => (res = r));
    http.expectOne(`${base}/admin-create`).flush({emailSent: false});

    expect(res.emailSent)
      .toBe(false);
  });

  it('regeneratePassword cible le bon utilisateur et remonte emailSent', () => {
    let res: any = null;
    service.regeneratePassword(42).subscribe(r => (res = r));

    const req = http.expectOne(`${base}/42/regenerate-password`);
    expect(req.request.method).toBe('POST');
    req.flush({emailSent: false});

    expect(res.emailSent).toBe(false);
  });

  it('list recupere la liste des utilisateurs', () => {
    let recu: any = null;
    service.list().subscribe(r => (recu = r));
    http.expectOne(base).flush([{id: 1, email: 'a@test.fr', role: 'USER'}]);

    expect(recu).toHaveLength(1);
  });

  it('delete utilise la methode DELETE', () => {
    service.delete(7).subscribe();
    const req = http.expectOne(`${base}/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});

describe('BackupService', () => {
  let service: BackupService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/admin/backup`;

  beforeEach(() => {
    TestBed.configureTestingModule({imports: [HttpClientTestingModule]});
    service = TestBed.inject(BackupService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('demande l archive en blob pour pouvoir la telecharger', () => {
    service.export(true, true).subscribe();

    const req = http.expectOne(`${base}/export?includeUploads=true&includeS3=true`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['zip']));
  });

  it('repercute les options decochees dans l URL', () => {
    service.export(false, false).subscribe();

    http.expectOne(`${base}/export?includeUploads=false&includeS3=false`)
      .flush(new Blob(['zip']));
  });

  it('demande la reponse complete, necessaire pour lire le nom de fichier', () => {
    let res: any = null;
    service.export(true, false).subscribe(r => (res = r));

    http.expectOne(`${base}/export?includeUploads=true&includeS3=false`).flush(
      new Blob(['zip']),
      {headers: {'Content-Disposition': 'attachment; filename="export-2026.zip"'}}
    );

    expect(res.headers.get('Content-Disposition')).toContain('export-2026.zip');
  });
});
