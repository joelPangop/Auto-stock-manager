import {Component, OnInit} from '@angular/core';
import {FormBuilder, Validators, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from '../../../services/auth.service';
import {switchMap} from 'rxjs/operators';
import {of} from 'rxjs';
import {UserService} from '../../../services/user.service';
import {User} from '../../../models/User';
import {Role} from '../../../models/enums/Role';

interface RoleOption { value: Role; label: string; }

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  loading = false;
  user: User;

  form: FormGroup = this.fb.group({
    nom: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: [''],
    role: ['USER', [Validators.required]]
  });

  id: number;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private snack: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        switchMap(pm => {
          const idStr = pm.get('id');
          if (!idStr) return of(null);
          this.id = Number(idStr);
          return this.userService.get(this.id);
        })
      )
      .subscribe(user => {
        if (!user) {
          this.form.reset({nom: '', email: '', phoneNumber: '', role: 'USER'});
          return;
        }
        this.user = user;
        this.form.patchValue({
          nom: user.nom ?? '',
          email: user.email ?? '',
          phoneNumber: user.phoneNumber ?? '',
          role: user.role ?? 'USER'
        });
      });
  }

  get roleOptions(): RoleOption[] {
    if (this.auth.isSuperAdmin()) {
      return [
        {value: 'SUPER_ADMIN', label: 'Super Admin'},
        {value: 'ADMIN', label: 'Admin'},
        {value: 'MANAGER', label: 'Manager'},
        {value: 'VENDEUR', label: 'Vendeur'},
        {value: 'USER', label: 'Utilisateur'}
      ];
    }
    return [
      {value: 'ADMIN', label: 'Admin'},
      {value: 'MANAGER', label: 'Manager'},
      {value: 'VENDEUR', label: 'Vendeur'},
      {value: 'USER', label: 'Utilisateur'}
    ];
  }

  submit() {
    if (this.form.invalid) return;
    this.loading = true;
    const {nom, email, phoneNumber, role} = this.form.value;

    if (!this.id) {
      this.userService.adminCreate({nom, email, phoneNumber, role}).subscribe({
        next: () => {
          this.loading = false;
          this.snack.open('Utilisateur cree. Un email avec le mot de passe temporaire a ete envoye.', 'OK', {duration: 4000});
          this.router.navigateByUrl('/users');
        },
        error: (e) => {
          this.loading = false;
          const msg = e?.error?.message || 'Echec de la creation';
          this.snack.open(msg, 'Fermer', {duration: 4000});
        }
      });
    } else {
      this.user.email = email;
      this.user.nom = nom;
      this.userService.update(this.user).subscribe({
        next: () => {
          this.loading = false;
          this.snack.open('Compte modifie', 'OK', {duration: 2000});
        },
        error: (e) => {
          this.loading = false;
          const msg = e?.error?.message || 'Echec de la modification';
          this.snack.open(msg, 'Fermer', {duration: 3000});
        }
      });
    }
  }

  backToList() {
    this.router.navigate(['/users']);
  }

  get f() { return this.form.controls; }
}
