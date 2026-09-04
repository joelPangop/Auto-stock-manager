import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateModule} from '@ngx-translate/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatDividerModule} from '@angular/material/divider';
import {MatIconModule} from '@angular/material/icon';
import {of} from 'rxjs';

import {VenteCreateDialogComponent} from './vente-create-dialog.component';
import {VenteService} from '../../../../services/vente.service';
import {ClientService} from '../../../../services/client.service';
import {UserService} from '../../../../services/user.service';
import {PaiementService} from '../../../../services/paiement.service';

/**
 * Creation d'une vente depuis la fiche vehicule.
 *
 * Le dialogue permet de creer un client sans quitter la vente. Ce chemin
 * dependait d'un nom de controle ecrit en dur, que rien ne reliait au
 * formulaire : la moindre divergence casse l'ecran en pleine saisie.
 */
describe('VenteCreateDialogComponent', () => {
  let fixture: ComponentFixture<VenteCreateDialogComponent>;
  let component: VenteCreateDialogComponent;
  let clientsSrv: any;
  let dialog: any;
  let apresFermeture: any;

  const clientInitial = {id: 1, nom: 'Client Un', email: 'un@test.fr'};
  const clientCree = {id: 7, nom: 'Client Sept', email: 'sept@test.fr'};

  beforeEach(async () => {
    apresFermeture = of(clientCree);
    clientsSrv = {list: jest.fn().mockReturnValue(of([clientInitial]))};
    dialog = {open: jest.fn(() => ({afterClosed: () => apresFermeture}))};

    await TestBed.configureTestingModule({
      declarations: [VenteCreateDialogComponent],
      imports: [
        ReactiveFormsModule, NoopAnimationsModule, TranslateModule.forRoot(),
        MatFormFieldModule, MatInputModule, MatSelectModule,
        MatDividerModule, MatIconModule, MatDialogModule,
      ],
      providers: [
        {provide: VenteService, useValue: {create: jest.fn().mockReturnValue(of({id: 3}))}},
        {provide: ClientService, useValue: clientsSrv},
        {provide: UserService, useValue: {list: () => of([])}},
        {provide: PaiementService, useValue: {create: jest.fn().mockReturnValue(of({}))}},
        {provide: MatDialog, useValue: dialog},
        {provide: MatDialogRef, useValue: {close: jest.fn()}},
        {provide: MAT_DIALOG_DATA, useValue: {idVoiture: 3, prixSuggere: 15000}},
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(VenteCreateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('ajout d un client depuis la vente', () => {
    it('ne leve pas d exception', () => {
      // Regression : le handler ecrivait dans controls['clientId'], alors que
      // le controle s'appelle idClient. controls['clientId'] valait undefined
      // et setValue levait « Cannot read properties of undefined ».
      expect(() => component.openNewClientDialog()).not.toThrow();
    });

    it('selectionne le client qui vient d etre cree', () => {
      component.openNewClientDialog();

      expect(component.form.value.idClient).toBe(7);
    });

    it('recharge la liste pour que le nouveau client ait une option', () => {
      clientsSrv.list.mockReturnValue(of([clientInitial, clientCree]));

      component.openNewClientDialog();

      expect(component.clients.map(c => c.id)).toContain(7);
    });

    it('ne touche pas au formulaire si la creation est annulee', () => {
      apresFermeture = of(null);
      component.form.patchValue({idClient: 1});

      component.openNewClientDialog();

      expect(component.form.value.idClient).toBe(1);
    });
  });

  describe('coherence des noms de controles', () => {
    it('expose exactement les controles attendus par le template', () => {
      expect(Object.keys(component.form.controls).sort()).toEqual(
        ['acompteMontant', 'dateVente', 'idClient', 'idVendeur', 'modePaiement', 'prixFinal']);
    });
  });
});
