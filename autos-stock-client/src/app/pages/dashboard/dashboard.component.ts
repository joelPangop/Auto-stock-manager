import { Component, OnInit } from '@angular/core';

// @Component({
//   selector: 'app-dashboard',
//   templateUrl: './dashboard.component.html',
//   styleUrls: ['./dashboard.component.scss']
// })
@Component({
  selector: 'app-dashboard',
  template: `<div style="padding:24px">Bienvenue 👋 — vous êtes connecté.</div>`
})
export class DashboardComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
