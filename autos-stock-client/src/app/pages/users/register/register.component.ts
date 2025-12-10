import { Component } from '@angular/core';
import { FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import {AuthService} from "../../../services/auth.service";

function matchPasswordValidator(group: AbstractControl): ValidationErrors | null {
  const p = group.get('password')?.value;
  const c = group.get('confirm')?.value;
  return p && c && p !== c ? { mismatch: true } : null;
}

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  hide = true;
  hide2 = true;
  loading = false;

  form = this.fb.group({
    nom: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    passwords: this.fb.group({
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirm:  ['', [Validators.required]]
    }, { validators: matchPasswordValidator })
  });

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private snack: MatSnackBar,
    private router: Router
  ) {}

  submit() {
    if (this.form.invalid) return;
    this.loading = true;
    const { nom, email } = this.form.value;
    const password = this.form.value.passwords?.password as string;

    this.auth.register({ nom: nom!, email: email!, password }).subscribe({
      next: () => {
        this.loading = false;
        this.snack.open('Compte créé — connecté ✔', 'OK', { duration: 2000 });
        this.router.navigateByUrl('/dashboard');
      },
      error: (e) => {
        this.loading = false;
        const msg = e?.error?.message || 'Échec de l’inscription';
        this.snack.open(msg, 'Fermer', { duration: 3000 });
      }
    });
  }

  get f() { return this.form.controls; }
  get pw() { return (this.form.get('passwords') as any).controls; }
  get mismatch() { return this.form.get('passwords')?.hasError('mismatch'); }
}
