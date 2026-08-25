import {TestBed} from '@angular/core/testing';
import {Router} from '@angular/router';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {AuthService} from './auth.service';
import {AdminGuard} from './AdminGuard';
import {SuperAdminGuard} from './SuperAdminGuard';

/**
 * Gardes de route.
 *
 * Ils ne constituent pas une protection : un utilisateur peut toujours appeler
 * l'API directement. La vraie barriere est cote serveur (@PreAuthorize et les
 * regles de SecurityConfig, couvertes par UserControllerVisibilityTest). Ces
 * tests verifient seulement qu'on n'affiche pas un ecran inutilisable, et que
 * la frontiere ADMIN / SUPER_ADMIN est bien la ou on la croit.
 */
describe('Gardes de route', () => {
  let router: {navigate: jest.Mock};
  let auth: AuthService;

  beforeEach(() => {
    localStorage.clear();
    router = {navigate: jest.fn()};

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [{provide: Router, useValue: router}],
    });
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => localStorage.clear());

  const connecter = (role: string | null) => {
    if (role === null) {
      localStorage.removeItem('user_token');
    } else {
      localStorage.setItem('user_token',
        JSON.stringify({id: 1, nom: 'T', email: 't@test.fr', role}));
    }
  };

  describe('AdminGuard', () => {
    let guard: AdminGuard;
    beforeEach(() => (guard = TestBed.inject(AdminGuard)));

    it.each(['ADMIN', 'SUPER_ADMIN'])('laisse passer un %s', role => {
      connecter(role);
      expect(guard.canActivate()).toBe(true);
      expect(router.navigate).not.toHaveBeenCalled();
    });

    it.each(['USER', 'VENDEUR', 'MANAGER'])('bloque un %s et le renvoie a l accueil', role => {
      connecter(role);
      expect(guard.canActivate()).toBe(false);
      expect(router.navigate).toHaveBeenCalledWith(['/']);
    });

    it('bloque un visiteur sans session', () => {
      connecter(null);
      expect(guard.canActivate()).toBe(false);
    });
  });

  describe('SuperAdminGuard', () => {
    let guard: SuperAdminGuard;
    beforeEach(() => (guard = TestBed.inject(SuperAdminGuard)));

    it('laisse passer un SUPER_ADMIN', () => {
      connecter('SUPER_ADMIN');
      expect(guard.canActivate()).toBe(true);
    });

    it('bloque un ADMIN — c est toute la difference avec AdminGuard', () => {
      connecter('ADMIN');
      expect(guard.canActivate()).toBe(false);
      expect(router.navigate).toHaveBeenCalledWith(['/']);
    });

    it('bloque un utilisateur ordinaire', () => {
      connecter('USER');
      expect(guard.canActivate()).toBe(false);
    });

    it('bloque un visiteur sans session', () => {
      connecter(null);
      expect(guard.canActivate()).toBe(false);
    });
  });
});
