import { Component, OnInit } from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {AuthService} from "../../../services/auth.service";
import {User} from "../../../models/User";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {

  ngOnInit(): void {
  }
  user: User | null;
  form = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(2)]],
    email: [{value: '', disabled: true}, [Validators.required, Validators.email]],
  });

  constructor(private fb: FormBuilder, private auth: AuthService) {
    this.user = this.auth.currentUser;
    if (this.user) {
      this.form.patchValue({ fullName: this.user.nom, email: this.user.email });
    }
  }

  save() {
    if (this.form.invalid || !this.user) return;
    // Appel API update profil (nom, avatar, etc.)
    // this.profileSrv.update(this.user.id, this.form.getRawValue()).subscribe(...)
  }
}
