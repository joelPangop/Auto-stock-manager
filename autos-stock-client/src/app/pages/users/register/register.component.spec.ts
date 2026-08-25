import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {of, throwError} from 'rxjs';

import {RegisterComponent} from './register.component';
import {AuthService} from '../../../services/auth.service';
import {UserService} from '../../../services/user.service';

/**
 * Ecran de creation / modification de compte par un administrateur.
 *
 * Deux regressions sont verrouillees ici :
 *  - roleOptions doit etre calcule une seule fois. En getter, il renvoyait un
 *    nouveau tableau a chaque cycle de detection de changement, les mat-option
 *    etaient detruites et recreees en boucle et la page se figeait sur mobile.
 *  - le message de fin doit refleter emailSent : annoncer un succes quand
 *    l'invitation n'est pas partie laisse un compte inaccessible.
 */
describe('RegisterComponent', () => {
  let fixture: ComponentFixture<RegisterComponent>;
  let component: RegisterComponent;
  let auth: any;
  let userService: any;
  let router: any;
  let snack: any;

  const construire = (role: string, paramId: string | null = null) => {
    auth = {
      isSuperAdmin: () => role === 'SUPER_ADMIN',
      isAdmin: () => role === 'ADMIN' || role === 'SUPER_ADMIN',
    };
    userService = {
      adminCreate: jest.fn().mockReturnValue(of({emailSent: true})),
      update: jest.fn().mockReturnValue(of({})),
      get: jest.fn().mockReturnValue(of(null)),
    };
    router = {navigateByUrl: jest.fn(), navigate: jest.fn()};
    snack = {open: jest.fn()};

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      // Le template est reellement rendu : le test de stabilite de reference
      // n'aurait aucun sens sans mat-select, puisque c'est la recreation en
      // boucle des <mat-option> qui figeait la page.
      imports: [
        ReactiveFormsModule, NoopAnimationsModule, MatCardModule, MatFormFieldModule,
        MatInputModule, MatSelectModule, MatButtonModule, MatIconModule,
        MatProgressSpinnerModule,
      ],
      providers: [
        {provide: AuthService, useValue: auth},
        {provide: UserService, useValue: userService},
        {provide: Router, useValue: router},
        {provide: MatSnackBar, useValue: snack},
        {provide: ActivatedRoute, useValue: {paramMap: of({get: () => paramId})}},
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  describe('roles proposes', () => {
    it('un SUPER_ADMIN peut attribuer le role SUPER_ADMIN', () => {
      construire('SUPER_ADMIN');
      expect(component.roleOptions.map(o => o.value)).toContain('SUPER_ADMIN');
    });

    it('un ADMIN ne peut pas attribuer le role SUPER_ADMIN', () => {
      construire('ADMIN');
      expect(component.roleOptions.map(o => o.value)).not.toContain('SUPER_ADMIN');
    });

    it('un ADMIN garde les autres roles', () => {
      construire('ADMIN');
      expect(component.roleOptions.map(o => o.value))
        .toEqual(expect.arrayContaining(['ADMIN', 'MANAGER', 'VENDEUR', 'USER']));
    });
  });

  describe('stabilite de reference (regression de la page figee)', () => {
    it('roleOptions garde la meme reference d une lecture a l autre', () => {
      construire('ADMIN');
      const premiere = component.roleOptions;

      for (let i = 0; i < 50; i++) {
        fixture.detectChanges();
      }

      expect(component.roleOptions)
        .toBe(premiere);
    });

    it('trackByRole identifie une option par sa valeur', () => {
      construire('ADMIN');
      expect(component.trackByRole(0, {value: 'ADMIN' as any, label: 'Admin'})).toBe('ADMIN');
    });
  });

  describe('creation', () => {
    const remplir = () => component.form.setValue({
      nom: 'Nouveau', email: 'n@test.fr', phoneNumber: '', role: 'USER',
    });

    it('appelle adminCreate avec les valeurs du formulaire', () => {
      construire('ADMIN');
      remplir();
      component.submit();

      expect(userService.adminCreate).toHaveBeenCalledWith(
        expect.objectContaining({nom: 'Nouveau', email: 'n@test.fr', role: 'USER'}));
    });

    it('n envoie rien si le formulaire est invalide', () => {
      construire('ADMIN');
      component.form.patchValue({email: 'pas-un-email'});
      component.submit();

      expect(userService.adminCreate).not.toHaveBeenCalled();
    });

    it('annonce l envoi quand emailSent est vrai', () => {
      construire('ADMIN');
      remplir();
      component.submit();

      const message = snack.open.mock.calls[0][0];
      expect(message).toContain('envoye');
    });

    it('avertit que le mot de passe est perdu quand emailSent est faux', () => {
      construire('ADMIN');
      userService.adminCreate.mockReturnValue(of({emailSent: false}));
      remplir();
      component.submit();

      const message = snack.open.mock.calls[0][0];
      expect(message).toContain('PAS');
      expect(message.toLowerCase()).toContain('perdu');
    });

    it('libere le bouton apres une erreur serveur', () => {
      construire('ADMIN');
      userService.adminCreate.mockReturnValue(throwError({error: {message: 'Email deja utilise'}}));
      remplir();
      component.submit();

      expect(component.loading).toBe(false);
    });
  });
});
