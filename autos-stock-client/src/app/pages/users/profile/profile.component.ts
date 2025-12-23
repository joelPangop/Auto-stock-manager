import { Component, OnInit } from '@angular/core';
import {AbstractControl, FormBuilder, ValidationErrors, Validators} from "@angular/forms";
import {AuthService} from "../../../services/auth.service";
import {User} from "../../../models/User";
import {BehaviorSubject} from "rxjs";
import {UserService} from "../../../services/user.service";
import {MatSnackBar} from "@angular/material/snack-bar";

function matchPasswordValidator(group: AbstractControl): ValidationErrors | null {
  const p = group.get('password')?.value;
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
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  form = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(2)]],
    email: [{value: '', disabled: true}, [Validators.required, Validators.email]],
    passwords: this.fb.group({
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirm: ['', [Validators.required]]
    }, {validators: matchPasswordValidator})
  });

  constructor(private fb: FormBuilder, private auth: AuthService, private userService: UserService, private snack: MatSnackBar,) {
    this.user = this.auth.currentUser;
    if (this.user) {
      this.form.patchValue({ fullName: this.user.nom, email: this.user.email });
    }
  }

  save() {
    if (this.form.invalid || !this.user) return;

    this.user.nom = this.form.value.fullName;
    const password = this.form.value.passwords?.password as string;
    this.user.password = password;

    this.userService.update(this.user).subscribe({
      next: () => {
        this.snack.open('Profile modifié ✔', 'OK', {duration: 2000});
        this.edit = false;
        this.auth.currentUser = this.user;
        this.refresh$.next();
        // this.router.navigateByUrl('/users');
      },
      error: (e) => {
        const msg = e?.error?.message || 'Échec de la modification';
        this.snack.open(msg, 'Fermer', {duration: 3000});
      }
    })
    // Appel API update profil (nom, avatar, etc.)
    // this.profileSrv.update(this.user.id, this.form.getRawValue()).subscribe(...)
  }

  get f() {
    return this.form.controls;
  }

  get pw() {
    return (this.form.get('passwords') as any).controls;
  }

  get mismatch() {
    return this.form.get('passwords')?.hasError('mismatch');
  }

  load() {
    this.refresh$.next();
  }
}
