import {TestBed} from '@angular/core/testing';
import {HttpClient, HTTP_INTERCEPTORS} from '@angular/common/http';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {of, throwError} from 'rxjs';

import {JwtInterceptor} from './jwt.interceptor';
import {TokenStorageService} from './token-storage.service';
import {AuthService} from './auth.service';

/**
 * Intercepteur HTTP : pose du jeton et renouvellement automatique.
 *
 * Deux pieges sont couverts ici. D'abord l'endpoint de connexion et celui de
 * refresh ne doivent PAS recevoir l'ancien jeton — sur /refresh, cela provoque
 * une boucle : 401, refresh, 401, refresh. Ensuite un refresh qui echoue doit
 * deconnecter, sinon l'utilisateur reste sur une session morte et enchaine les
 * 401 sur chaque ecran.
 */
describe('JwtInterceptor', () => {
  let http: HttpClient;
  let mock: HttpTestingController;
  let tokens: {access: string | null};
  let auth: {refresh: jest.Mock; logout: jest.Mock};

  /**
   * Reconstruit le module avec le jeton voulu. L'intercepteur capture le
   * TokenStorageService a la construction de l'injecteur : muter le mock apres
   * l'injection n'aurait aucun effet sur l'instance deja creee.
   */
  const configurer = (jeton: string | null = 'Bearer jeton-valide') => {
    tokens = {access: jeton};
    auth = {refresh: jest.fn(), logout: jest.fn()};

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        {provide: TokenStorageService, useValue: tokens},
        {provide: AuthService, useValue: auth},
        {provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true},
      ],
    });

    http = TestBed.inject(HttpClient);
    mock = TestBed.inject(HttpTestingController);
  };

  beforeEach(() => configurer());

  afterEach(() => mock.verify());

  describe('pose du jeton', () => {
    it('ajoute l en-tete Authorization sur un appel metier', () => {
      http.get('/api/voitures').subscribe();

      const req = mock.expectOne('/api/voitures');
      expect(req.request.headers.get('Authorization')).toBe('Bearer jeton-valide');
      req.flush({});
    });

    it('n ajoute rien quand aucun jeton n est stocke', () => {
      configurer(null);
      http.get('/api/voitures').subscribe();

      const req = mock.expectOne('/api/voitures');
      expect(req.request.headers.has('Authorization')).toBe(false);
      req.flush({});
    });

    it('n envoie pas l ancien jeton sur /auth/login', () => {
      http.post('/api/auth/login', {}).subscribe();

      const req = mock.expectOne('/api/auth/login');
      expect(req.request.headers.has('Authorization')).toBe(false);
      req.flush({});
    });

    it('n envoie pas l ancien jeton sur /auth/refresh', () => {
      // Sans cette exclusion, un jeton expire est renvoye a /refresh, qui
      // repond 401, ce qui declenche un nouveau refresh : boucle infinie.
      http.post('/api/auth/refresh', {}).subscribe();

      const req = mock.expectOne('/api/auth/refresh');
      expect(req.request.headers.has('Authorization')).toBe(false);
      req.flush({});
    });

    it('pose le jeton sur les autres endpoints d authentification', () => {
      http.post('/api/auth/change-password', {}).subscribe();

      const req = mock.expectOne('/api/auth/change-password');
      expect(req.request.headers.get('Authorization')).toBe('Bearer jeton-valide');
      req.flush({});
    });
  });

  describe('renouvellement sur 401', () => {
    it('tente un refresh puis rejoue la requete avec le nouveau jeton', () => {
      auth.refresh.mockReturnValue(of({accessToken: 'jeton-neuf', tokenType: 'Bearer'}));

      let recu: any = null;
      http.get('/api/voitures').subscribe(r => (recu = r));

      mock.expectOne('/api/voitures').flush(null, {status: 401, statusText: 'Unauthorized'});

      expect(auth.refresh).toHaveBeenCalled();

      const rejoue = mock.expectOne('/api/voitures');
      expect(rejoue.request.headers.get('Authorization')).toBe('Bearer jeton-neuf');
      rejoue.flush({donnees: 'ok'});

      expect(recu).toEqual({donnees: 'ok'});
    });

    it('utilise Bearer par defaut si le serveur ne precise pas le type', () => {
      auth.refresh.mockReturnValue(of({accessToken: 'jeton-neuf'}));

      http.get('/api/voitures').subscribe();
      mock.expectOne('/api/voitures').flush(null, {status: 401, statusText: 'Unauthorized'});

      const rejoue = mock.expectOne('/api/voitures');
      expect(rejoue.request.headers.get('Authorization')).toBe('Bearer jeton-neuf');
      rejoue.flush({});
    });

    it('deconnecte quand le refresh echoue', () => {
      auth.refresh.mockReturnValue(throwError({status: 401}));

      http.get('/api/voitures').subscribe({error: () => {}});
      mock.expectOne('/api/voitures').flush(null, {status: 401, statusText: 'Unauthorized'});

      expect(auth.logout)
        .toHaveBeenCalled();
    });

    it('ne tente aucun refresh sur un 401 de la page de connexion', () => {
      http.post('/api/auth/login', {}).subscribe({error: () => {}});
      mock.expectOne('/api/auth/login').flush(null, {status: 401, statusText: 'Unauthorized'});

      expect(auth.refresh)
        .not.toHaveBeenCalled();
    });

    it('ne tente aucun refresh sur un code autre que 401', () => {
      http.get('/api/voitures').subscribe({error: () => {}});
      mock.expectOne('/api/voitures').flush(null, {status: 500, statusText: 'Server Error'});

      expect(auth.refresh).not.toHaveBeenCalled();
    });
  });

  describe('normalisation des erreurs', () => {
    it('remonte le message du serveur quand il y en a un', () => {
      let erreur: any = null;
      http.get('/api/voitures').subscribe({error: e => (erreur = e)});

      mock.expectOne('/api/voitures')
        .flush({message: 'Vehicule introuvable'}, {status: 404, statusText: 'Not Found'});

      expect(erreur.error.message).toBe('Vehicule introuvable');
      expect(erreur.status).toBe(404);
    });

    it('fournit un message par defaut quand le serveur n en donne aucun', () => {
      let erreur: any = null;
      http.get('/api/voitures').subscribe({error: e => (erreur = e)});

      mock.expectOne('/api/voitures').flush(null, {status: 500, statusText: 'Server Error'});

      expect(erreur.error.message).toBe('Erreur inconnue');
    });

    it('conserve le code HTTP d origine', () => {
      let erreur: any = null;
      http.get('/api/voitures').subscribe({error: e => (erreur = e)});

      mock.expectOne('/api/voitures').flush({message: 'Trop de requetes'},
        {status: 429, statusText: 'Too Many Requests'});

      expect(erreur.status).toBe(429);
    });
  });
});
