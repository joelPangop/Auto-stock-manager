import {Component, Inject, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {AuthService} from "../../../../services/auth.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {User} from "../../../../models/User";

@Component({
  selector: 'app-profile-dialog',
  templateUrl: './profile-dialog.component.html',
  styleUrls: ['./profile-dialog.component.scss']
})
export class ProfileDialogComponent implements OnInit {

  user: User | null;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: User | null,
    private auth: AuthService,
    private router: Router,
    private ref: MatDialogRef<ProfileDialogComponent>
  ) {
    this.user = data ?? this.auth.currentUser;
  }

  ngOnInit(): void {
  }

  gotoProfile() {
    this.ref.close();
    this.router.navigate(['/profile']);
  }

  gotoSettings() {
    this.ref.close();
    this.router.navigate(['/parametres']);
  }

  logout() {
    this.ref.close();
    this.auth.logout();
  }
}
