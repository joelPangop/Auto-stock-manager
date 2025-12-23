import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {LoginComponent} from "./pages/users/login/login.component";
import {DashboardComponent} from "./pages/dashboard/dashboard.component";
import {AuthGuard} from "./services/auth.guard";
import {LayoutComponent} from "./pages/layout/layout.component";
import {RegisterComponent} from "./pages/users/register/register.component";
import {VoituresListComponent} from "./pages/voitures/voitures-list/voitures-list.component";
import {VoitureDetailComponent} from "./pages/voitures/voiture-detail/voiture-detail.component";
import {VentesComponent} from "./pages/ventes/ventes.component";
import {ClientsComponent} from "./pages/clients/clients.component";
import {EntretiensComponent} from "./pages/entretiens/entretiens.component";
import {DocumentsComponent} from "./pages/documents/documents.component";
import {MouvementsComponent} from "./pages/mouvements/mouvements.component";
import {PaiementsComponent} from "./pages/paiements/paiements.component";
import {ProfileComponent} from "./pages/users/profile/profile.component";
import {SettingsComponent} from "./pages/users/settings/settings.component";
import {AdminGuard} from "./services/AdminGuard";
import {UsersComponent} from "./pages/users/admin/users/users.component";

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'voitures', component: VoituresListComponent },
      { path: 'voitures/:id', component: VoitureDetailComponent,
        runGuardsAndResolvers: 'always'},
      { path: 'ventes', component: VentesComponent },
      { path: 'clients', component: ClientsComponent },
      { path: 'entretiens', component: EntretiensComponent },
      { path: 'documents', component: DocumentsComponent },
      { path: 'mouvements', component: MouvementsComponent },
      { path: 'paiements', component: PaiementsComponent },
      { path: 'profile', component: ProfileComponent },
      { path: 'parametres', component: SettingsComponent },
      { path: 'users', component: UsersComponent, canActivate: [AdminGuard] },
      { path: 'register', component: RegisterComponent, canActivate: [AdminGuard] },   // ⬅️ ajouté
      { path: 'register/:id', component: RegisterComponent, canActivate: [AdminGuard] },   // ⬅️ ajouté
    ]
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    onSameUrlNavigation: 'reload',          // recharger si même URL
    scrollPositionRestoration: 'enabled',
    anchorScrolling: 'enabled'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
