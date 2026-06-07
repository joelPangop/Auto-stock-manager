import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent {
  loading = false;

  form: FormGroup = this.fb.group({
    identifier: ['', [Validators.required, Validators.minLength(3)]],
    deliveryMethod: ['EMAIL', Validators.required]
  });

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private snack: MatSnackBar
  ) {}

  get isPhone(): boolean {
    const v: string = this.form.value.identifier ?? '';
    return v.startsWith('+') || /^\d{8,}$/.test(v.replace(/\s/g, ''));
  }

  onIdentifierChange(): void {
    if (this.isPhone) {
      this.form.patchValue({ deliveryMethod: 'SMS' });
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const { identifier, deliveryMethod } = this.form.value;
    this.auth.forgotPassword({ identifier, deliveryMethod }).subscribe({
      next: () => {
        this.loading = false;
        this.snack.open('Code envoyé !', 'OK', { duration: 3000 });
        this.router.navigate(['/reset-password'], { queryParams: { identifier } });
      },
      error: (e) => {
        this.loading = false;
        const msg = e?.error?.message || 'Aucun compte trouvé avec cet identifiant.';
        this.snack.open(msg, 'Fermer', { duration: 4000 });
      }
    });
  }
}
