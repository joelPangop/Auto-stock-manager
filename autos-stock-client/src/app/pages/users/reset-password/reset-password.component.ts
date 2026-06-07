import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../services/auth.service';

function matchPasswordValidator(group: AbstractControl): ValidationErrors | null {
  const p = group.get('newPassword')?.value;
  const c = group.get('confirm')?.value;
  return p && c && p !== c ? { mismatch: true } : null;
}

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {
  loading = false;
  hideNew = true;
  hideConfirm = true;

  form: FormGroup = this.fb.group({
    identifier: ['', [Validators.required, Validators.minLength(3)]],
    code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6), Validators.pattern(/^\d{6}$/)]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirm: ['', Validators.required]
  }, { validators: matchPasswordValidator });

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    const identifier = this.route.snapshot.queryParamMap.get('identifier') ?? '';
    this.form.patchValue({ identifier });
  }

  get isPhone(): boolean {
    const v: string = this.form.value.identifier ?? '';
    return v.startsWith('+') || /^\d{8,}$/.test(v.replace(/\s/g, ''));
  }

  get mismatch(): boolean {
    return this.form.hasError('mismatch') && !!this.form.get('confirm')?.dirty;
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const { identifier, code, newPassword } = this.form.value;
    this.auth.resetPassword({ identifier, code, newPassword }).subscribe({
      next: () => {
        this.loading = false;
        this.snack.open('Mot de passe réinitialisé avec succès !', 'OK', { duration: 3000 });
        this.router.navigateByUrl('/login');
      },
      error: (e) => {
        this.loading = false;
        const msg = e?.error?.message || 'Code invalide ou expiré.';
        this.snack.open(msg, 'Fermer', { duration: 4000 });
      }
    });
  }
}
