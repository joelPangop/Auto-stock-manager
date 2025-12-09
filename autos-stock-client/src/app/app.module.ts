import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './pages/login/login.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCardModule} from "@angular/material/card";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatButtonModule} from "@angular/material/button";
import {MatInputModule} from "@angular/material/input";
import {MatIconModule} from "@angular/material/icon";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import {RouterModule} from "@angular/router";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {MatToolbarModule} from "@angular/material/toolbar";
import {LayoutComponent} from "./pages/layout/layout.component";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatListModule} from "@angular/material/list";
import {MatDividerModule} from "@angular/material/divider";
import {MatMenuModule} from "@angular/material/menu";
import { RegisterComponent } from './pages/register/register.component';
import { VoituresListComponent } from './pages/voitures/voitures-list/voitures-list.component';
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatSortModule} from "@angular/material/sort";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatTableModule} from "@angular/material/table";
import { VentesComponent } from './pages/ventes/ventes.component';
import { ClientsComponent } from './pages/clients/clients.component';
import { EntretiensComponent } from './pages/entretiens/entretiens.component';
import { DocumentsComponent } from './pages/documents/documents.component';
import { MouvementsComponent } from './pages/mouvements/mouvements.component';
import { PaiementsComponent } from './pages/paiements/paiements.component';
import { VoitureDetailComponent } from './pages/voitures/voiture-detail/voiture-detail.component';
import {MatTabsModule} from "@angular/material/tabs";
import {MatSelectModule} from "@angular/material/select";
import {JwtInterceptor} from "./services/jwt.interceptor";
import { VenteCreateDialogComponent } from './pages/features/ventes/vente-create-dialog/vente-create-dialog.component';
import {MatDialogModule} from "@angular/material/dialog";
import { VoitureCreateDialogComponent } from './pages/features/voitures/voiture-create-dialog/voiture-create-dialog.component';
import {MatCheckboxModule} from "@angular/material/checkbox";
import { MarqueCreateDialogComponent } from './pages/features/catalog/marque-create-dialog/marque-create-dialog.component';
import { ModeleCreateDialogComponent } from './pages/features/catalog/modele-create-dialog/modele-create-dialog.component';
import { FournisseurCreateDialogComponent } from './pages/features/fournisseur/fournisseur-create-dialog/fournisseur-create-dialog.component';
import { EntretienEditDialogComponent } from './pages/features/entretien/entretien-edit-dialog/entretien-edit-dialog.component';
import { MouvementEditDialogComponent } from './pages/features/mouvement/mouvement-edit-dialog/mouvement-edit-dialog.component';
import { DocumentUploadDialogComponent } from './pages/features/document/document-upload-dialog/document-upload-dialog.component';
import { ClientCreateDialogComponent } from './pages/features/client/client-create-dialog/client-create-dialog.component';
import { DocumentEditDialogComponent } from './pages/features/document/document-edit-dialog/document-edit-dialog-component';
import {MatSlideToggleModule} from "@angular/material/slide-toggle";

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    LayoutComponent,
    DashboardComponent,
    RegisterComponent,
    VoituresListComponent,
    VentesComponent,
    ClientsComponent,
    EntretiensComponent,
    DocumentsComponent,
    MouvementsComponent,
    PaiementsComponent,
    VoitureDetailComponent,
    VenteCreateDialogComponent,
    VoitureCreateDialogComponent,
    MarqueCreateDialogComponent,
    ModeleCreateDialogComponent,
    FournisseurCreateDialogComponent,
    EntretienEditDialogComponent,
    MouvementEditDialogComponent,
    DocumentUploadDialogComponent,
    ClientCreateDialogComponent,
    DocumentEditDialogComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MatToolbarModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    MatSidenavModule, MatListModule, MatDividerModule, MatMenuModule,
    MatTableModule, MatPaginatorModule, MatSortModule, MatTooltipModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    RouterModule,
    HttpClientModule, MatTabsModule, MatSelectModule, MatDialogModule, MatCheckboxModule, MatSlideToggleModule
  ],
  providers: [{ provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true }],
  bootstrap: [AppComponent]
})
export class AppModule { }
