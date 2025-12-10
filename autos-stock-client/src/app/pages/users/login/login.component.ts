import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import {AuthService} from "../../../services/auth.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  hide = true;
  loading = false;
  isAdmin = false;

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private snack: MatSnackBar,
    private router: Router
  ) {
    this.isAdmin = this.auth.isAdmin();
  }

  submit() {
    if (this.form.invalid) return;
    this.loading = true;
    this.auth.login(this.form.value as any).subscribe({
      next: () => {
        this.loading = false;
        this.snack.open('Connexion réussie', 'OK', { duration: 2000 });
        this.router.navigateByUrl('/dashboard');
      },
      error: () => {
        this.loading = false;
        this.snack.open('Identifiants invalides', 'Fermer', { duration: 3000 });
      }
    });
  }
}
