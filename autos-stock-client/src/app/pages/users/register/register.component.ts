import {Component, OnInit} from '@angular/core';
import {FormBuilder, Validators, AbstractControl, ValidationErrors, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from "../../../services/auth.service";
import {filter, map, shareReplay, switchMap, take, tap} from 'rxjs/operators';
import {BehaviorSubject, combineLatest, Observable, of, Subject} from 'rxjs';
import {UserService} from "../../../services/user.service";
import {User} from "../../../models/User";

function matchPasswordValidator(group: AbstractControl): ValidationErrors | null {
  const p = group.get('password')?.value;
  const c = group.get('confirm')?.value;
  return p && c && p !== c ? {mismatch: true} : null;
}

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  hide = true;
  hide2 = true;
  loading = false;
  user: User;

  form: FormGroup = this.fb.group({
    nom: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    passwords: this.fb.group({
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirm: ['', [Validators.required]]
    }, {validators: matchPasswordValidator})
  });
  private readonly refresh$ = new BehaviorSubject<void>(undefined);
  id: number;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private snack: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute,
    private userService: UserService
  ) {
  }

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        switchMap(pm => {
          const idStr = pm.get('id');
          if (!idStr) return of(null);            // création
          this.id = Number(idStr);
          return this.userService.get(this.id);        // édition
        })
      )
      .subscribe(user => {
        if (!user) {
          // mode création
          this.form.reset();
          return;
        }

        this.user = user;
        // mode édition: on remplit le form
        this.form.patchValue({
          nom: user.nom ?? '',
          email: user.email ?? ''
        });

        // en édition, souvent on ne veut pas forcer le password
        this.form.get('passwords')?.reset();
      });
  }

  readonly id$ = this.route.paramMap.pipe(
    map(pm => Number(pm.get('id')))
  );

  submit() {

    if (!this.id) {
      if (this.form.invalid) return;
      this.loading = true;
      const {nom, email} = this.form.value;
      const password = this.form.value.passwords?.password as string;

      this.auth.register({nom: nom!, email: email!, password}).subscribe({
        next: () => {
          this.loading = false;
          this.snack.open('Compte créé ✔', 'OK', {duration: 2000});
          this.router.navigateByUrl('/users');
        },
        error: (e) => {
          this.loading = false;
          const msg = e?.error?.message || 'Échec de l’inscription';
          this.snack.open(msg, 'Fermer', {duration: 3000});
        }
      });
    } else {

      this.user.email = this.form.value.email;
      this.user.nom = this.form.value.nom;

      this.userService.update(this.user).subscribe({
        next: () => {
          this.loading = false;
          this.snack.open('Compte modifié ✔', 'OK', {duration: 2000});
          // this.router.navigateByUrl('/users');
        },
        error: (e) => {
          this.loading = false;
          const msg = e?.error?.message || 'Échec de la modification';
          this.snack.open(msg, 'Fermer', {duration: 3000});
        }
      })
    }
  }

  backToList() {
    this.router.navigate(['/users']);
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
}
