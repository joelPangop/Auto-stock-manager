import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {AuthService} from './auth.service';
import {TokenStorageService} from './token-storage.service';
import {environment} from '../../environments/environment';

/**
 * Service d'authentification cote navigateur.
 *
 * Deux points meritent une attention particuliere :
 *  - le getter currentUser est appele depuis les templates, donc a chaque cycle
 *    de detection de changement. S'il re-emet systematiquement sur son
 *    BehaviorSubject, la page se fige (c'est le bug qui bloquait la creation
 *    d'utilisateur sur mobile). Les tests ci-dessous verrouillent la mise en
 *    cache qui l'a corrige.
 *  - changePassword doit taper le bon endpoint : l'ancien formulaire envoyait
 *    le mot de passe a PUT /users/{id}, qui le jetait en silence.
 */
describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/auth`;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService, TokenStorageService],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  const utilisateur = (role: string) => ({id: 1, nom: 'Test', email: 't@test.fr', role});

  describe('connexion', () => {
    it('enregistre les jetons et l utilisateur retourne', () => {
      let recu: any = null;
      service.login({email: 't@test.fr', password: 'x'} as any).subscribe(r => (recu = r));

      const req = http.expectOne(`${base}/login`);
      expect(req.request.method).toBe('POST');
      req.flush({
        accessToken: 'jeton-acces',
        refreshToken: 'jeton-refresh',
        tokenType: 'Bearer',
        user: utilisateur('ADMIN'),
      });

      expect(recu.accessToken).toBe('jeton-acces');
      expect(localStorage.getItem('access_token')).toBe('Bearer jeton-acces');
      expect(localStorage.getItem('refresh_token')).toBe('jeton-refresh');
    });

    it('logout efface tout le stockage local', () => {
      localStorage.setItem('access_token', 'x');
      localStorage.setItem('refresh_token', 'y');
      localStorage.setItem('user_token', JSON.stringify(utilisateur('USER')));

      service.logout();

      expect(localStorage.getItem('access_token')).toBeNull();
      expect(localStorage.getItem('refresh_token')).toBeNull();
      expect(localStorage.getItem('user_token')).toBeNull();
    });
  });

  describe('roles', () => {
    const connecter = (role: string) =>
      localStorage.setItem('user_token', JSON.stringify(utilisateur(role)));

    it('ADMIN est admin mais pas super admin', () => {
      connecter('ADMIN');
      expect(service.isAdmin()).toBe(true);
      expect(service.isSuperAdmin()).toBe(false);
    });

    it('SUPER_ADMIN est admin et super admin', () => {
      connecter('SUPER_ADMIN');
      expect(service.isAdmin()).toBe(true);
      expect(service.isSuperAdmin()).toBe(true);
    });

    it('un role ordinaire n est ni l un ni l autre', () => {
      connecter('VENDEUR');
      expect(service.isAdmin()).toBe(false);
      expect(service.isSuperAdmin()).toBe(false);
    });

    it('sans session, aucun privilege', () => {
      expect(service.isAdmin()).toBe(false);
      expect(service.isSuperAdmin()).toBe(false);
    });
  });

  describe('getter currentUser (regression de la page figee)', () => {
    it('ne re-emet pas tant que le stockage local n a pas change', () => {
      localStorage.setItem('user_token', JSON.stringify(utilisateur('ADMIN')));

      let emissions = 0;
      service.user$().subscribe(() => emissions++);
      const emissionsInitiales = emissions;

      // Simule une centaine de cycles de detection de changement : un template
      // lisant isAdmin() declenche cette lecture a chaque passe.
      for (let i = 0; i < 100; i++) {
        service.currentUser;
      }

      expect(emissions - emissionsInitiales)
        .toBeLessThanOrEqual(1);
    });

    it('re-emet quand le stockage local change reellement', () => {
      localStorage.setItem('user_token', JSON.stringify(utilisateur('USER')));
      service.currentUser;

      localStorage.setItem('user_token', JSON.stringify(utilisateur('ADMIN')));
      const apres = service.currentUser;

      expect(apres.role).toBe('ADMIN');
    });

    it('tolere un contenu illisible sans lever d exception', () => {
      localStorage.setItem('user_token', 'ceci-n-est-pas-du-json');

      expect(() => service.currentUser).not.toThrow();
      expect(service.currentUser).toBeNull();
    });
  });

  describe('changement de mot de passe', () => {
    it('appelle POST /auth/change-password avec les deux mots de passe', () => {
      service.changePassword({currentPassword: 'ancien', newPassword: 'nouveau'}).subscribe();

      const req = http.expectOne(`${base}/change-password`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({currentPassword: 'ancien', newPassword: 'nouveau'});
      req.flush(null);
    });

    it('remonte l erreur du serveur quand le mot de passe actuel est faux', () => {
      let statut = 0;
      service.changePassword({currentPassword: 'faux', newPassword: 'nouveau'})
        .subscribe({error: e => (statut = e.status)});

      http.expectOne(`${base}/change-password`)
        .flush({message: 'Mot de passe actuel incorrect'}, {status: 400, statusText: 'Bad Request'});

      expect(statut).toBe(400);
    });
  });

  describe('mot de passe oublie', () => {
    it('envoie l identifiant et le canal choisi', () => {
      service.forgotPassword({identifier: 't@test.fr', deliveryMethod: 'EMAIL'}).subscribe();

      const req = http.expectOne(`${base}/forgot-password`);
      expect(req.request.body).toEqual({identifier: 't@test.fr', deliveryMethod: 'EMAIL'});
      req.flush(null);
    });

    it('reinitialise avec le code recu', () => {
      service.resetPassword({identifier: 't@test.fr', code: '123456', newPassword: 'nouveau'})
        .subscribe();

      const req = http.expectOne(`${base}/reset-password`);
      expect(req.request.body.code).toBe('123456');
      req.flush(null);
    });
  });
});
