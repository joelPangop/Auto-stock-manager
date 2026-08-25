import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ReactiveFormsModule, FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {of, throwError} from 'rxjs';

import {ProfileComponent} from './profile.component';
import {AuthService} from '../../../services/auth.service';
import {UserService} from '../../../services/user.service';

/**
 * Page « Mon profil » — changement de mot de passe par l'utilisateur.
 *
 * Historique du bug couvert ici : le formulaire envoyait le mot de passe a
 * PUT /users/{id}, dont le DTO ne le porte pas. Il etait jete en silence et
 * l'ecran affichait « Profil modifie ». Ces tests verrouillent l'appel au bon
 * endpoint et la deconnexion qui suit, la session courante etant revoquee par
 * le serveur des que le mot de passe change.
 */
describe('ProfileComponent', () => {
  let fixture: ComponentFixture<ProfileComponent>;
  let component: ProfileComponent;
  let auth: any;
  let userService: any;
  let router: any;
  let snack: any;

  beforeEach(async () => {
    auth = {
      currentUser: {id: 1, nom: 'Jean', email: 'jean@test.fr', role: 'USER'},
      changePassword: jest.fn().mockReturnValue(of(null)),
      logout: jest.fn(),
    };
    userService = {update: jest.fn().mockReturnValue(of({}))};
    router = {navigateByUrl: jest.fn()};
    snack = {open: jest.fn()};

    await TestBed.configureTestingModule({
      declarations: [ProfileComponent],
      imports: [ReactiveFormsModule, FormsModule],
      providers: [
        {provide: AuthService, useValue: auth},
        {provide: UserService, useValue: userService},
        {provide: Router, useValue: router},
        {provide: MatSnackBar, useValue: snack},
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
  });

  const remplir = (actuel: string, nouveau: string, confirmation: string) =>
    component.passwordForm.setValue({
      currentPassword: actuel,
      newPassword: nouveau,
      confirm: confirmation,
    });

  describe('validation du formulaire', () => {
    it('est invalide tant qu il est vide', () => {
      expect(component.passwordForm.valid).toBe(false);
    });

    it('exige le mot de passe actuel', () => {
      remplir('', 'nouveau123', 'nouveau123');
      expect(component.passwordForm.valid).toBe(false);
    });

    it('refuse un nouveau mot de passe de moins de 6 caracteres', () => {
      remplir('ancien', 'court', 'court');
      expect(component.passwordForm.valid).toBe(false);
    });

    it('refuse deux saisies differentes', () => {
      remplir('ancien', 'nouveau123', 'nouveau124');
      expect(component.mismatch).toBe(true);
      expect(component.passwordForm.valid).toBe(false);
    });

    it('accepte une saisie coherente', () => {
      remplir('ancien', 'nouveau123', 'nouveau123');
      expect(component.passwordForm.valid).toBe(true);
    });
  });

  describe('soumission', () => {
    it('appelle le service avec l ancien et le nouveau mot de passe', () => {
      remplir('ancien123', 'nouveau123', 'nouveau123');
      component.changePassword();

      expect(auth.changePassword).toHaveBeenCalledWith({
        currentPassword: 'ancien123',
        newPassword: 'nouveau123',
      });
    });

    it('n envoie rien si le formulaire est invalide', () => {
      remplir('', '', '');
      component.changePassword();

      expect(auth.changePassword).not.toHaveBeenCalled();
    });

    it('ne passe jamais par PUT /users pour le mot de passe', () => {
      remplir('ancien123', 'nouveau123', 'nouveau123');
      component.changePassword();

      expect(userService.update)
        .not.toHaveBeenCalled();
    });

    it('deconnecte et renvoie vers la connexion apres un succes', () => {
      remplir('ancien123', 'nouveau123', 'nouveau123');
      component.changePassword();

      expect(auth.logout).toHaveBeenCalled();
      expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
    });

    it('vide le formulaire apres un succes', () => {
      remplir('ancien123', 'nouveau123', 'nouveau123');
      component.changePassword();

      expect(component.passwordForm.value.newPassword).toBeFalsy();
    });

    it('affiche le message du serveur en cas d echec et ne deconnecte pas', () => {
      auth.changePassword.mockReturnValue(
        throwError({error: {message: 'Mot de passe actuel incorrect'}}));

      remplir('faux', 'nouveau123', 'nouveau123');
      component.changePassword();

      expect(snack.open).toHaveBeenCalledWith(
        'Mot de passe actuel incorrect', 'Fermer', expect.anything());
      expect(auth.logout)
        .not.toHaveBeenCalled();
      expect(router.navigateByUrl).not.toHaveBeenCalled();
    });

    it('libere le bouton apres un echec', () => {
      auth.changePassword.mockReturnValue(throwError({status: 400}));

      remplir('faux', 'nouveau123', 'nouveau123');
      component.changePassword();

      expect(component.changingPassword).toBe(false);
    });
  });

  describe('modification du profil', () => {
    it('n envoie plus de mot de passe dans la mise a jour du profil', () => {
      component.form.patchValue({fullName: 'Jean Nouveau'});
      component.save();

      const envoye = userService.update.mock.calls[0][0];
      expect(envoye.password).toBeUndefined();
      expect(envoye.nom).toBe('Jean Nouveau');
    });
  });
});
