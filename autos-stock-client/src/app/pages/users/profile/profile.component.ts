import { Component, OnInit } from '@angular/core';
import {AbstractControl, FormBuilder, ValidationErrors, Validators} from "@angular/forms";
import {AuthService} from "../../../services/auth.service";
import {User} from "../../../models/User";
import {BehaviorSubject} from "rxjs";
import {UserService} from "../../../services/user.service";
import {MatSnackBar} from "@angular/material/snack-bar";

function matchPasswordValidator(group: AbstractControl): ValidationErrors | null {
  const p = group.get('newPassword')?.value;
  const c = group.get('confirm')?.value;
  return p && c && p !== c ? {mismatch: true} : null;
}

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {

  ngOnInit(): void {
  }

  user: User | null;
  edit = false;
  hide = true;
  hide2 = true;
  hideCurrent = true;
  changingPassword = false;
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  form = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(2)]],
    email: [{value: '', disabled: true}, [Validators.required, Validators.email]]
  });

  // Formulaire distinct : changer son mot de passe et modifier son nom sont
  // deux operations independantes, sur deux endpoints differents. Les garder
  // dans le meme formulaire obligeait a saisir un mot de passe pour pouvoir
  // enregistrer un simple changement de nom.
  passwordForm = this.fb.group({
    currentPassword: ['', [Validators.required]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirm: ['', [Validators.required]]
  }, {validators: matchPasswordValidator});

  constructor(private fb: FormBuilder, private auth: AuthService, private userService: UserService, private snack: MatSnackBar,) {
    this.user = this.auth.currentUser;
    if (this.user) {
      this.form.patchValue({ fullName: this.user.nom, email: this.user.email });
    }
  }

  save() {
    if (this.form.invalid || !this.user) return;

    this.user.nom = this.form.value.fullName;

    this.userService.update(this.user).subscribe({
      next: () => {
        this.snack.open('Profil modifié ✔', 'OK', {duration: 2000});
        this.edit = false;
        this.auth.currentUser = this.user;
        this.refresh$.next();
      },
      error: (e) => {
        const msg = e?.error?.message || 'Échec de la modification';
        this.snack.open(msg, 'Fermer', {duration: 3000});
      }
    })
  }

  changePassword() {
    if (this.passwordForm.invalid) return;
    this.changingPassword = true;

    const {currentPassword, newPassword} = this.passwordForm.value;

    this.auth.changePassword({
      currentPassword: currentPassword as string,
      newPassword: newPassword as string
    }).subscribe({
      next: () => {
        this.changingPassword = false;
        this.passwordForm.reset();
        this.snack.open('Mot de passe modifié ✔ Utilisez-le à votre prochaine connexion.', 'OK', {duration: 5000});
      },
      error: (e) => {
        this.changingPassword = false;
        const msg = e?.error?.message || 'Échec du changement de mot de passe';
        this.snack.open(msg, 'Fermer', {duration: 5000});
      }
    });
  }

  get f() {
    return this.form.controls;
  }

  get pw() {
    return this.passwordForm.controls;
  }

  get mismatch() {
    return this.passwordForm.hasError('mismatch');
  }

  load() {
    this.refresh$.next();
  }
}
