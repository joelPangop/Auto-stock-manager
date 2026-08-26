import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {TranslateModule} from '@ngx-translate/core';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatTooltipModule} from '@angular/material/tooltip';

// mat-table s appuie sur des directives structurelles (*matHeaderRowDef,
// *matRowDef) que NO_ERRORS_SCHEMA ne couvre pas : il faut les vrais modules.
const MATERIEL = [
  NoopAnimationsModule, MatTableModule, MatPaginatorModule, MatSortModule,
  MatIconModule, MatButtonModule, MatCardModule, MatTooltipModule,
  TranslateModule.forRoot(),
];
import {of} from 'rxjs';

import {ClientsComponent} from './clients.component';
import {ClientService} from '../../services/client.service';
import {AuthService} from '../../services/auth.service';

import {VentesComponent} from '../ventes/ventes.component';
import {VenteService} from '../../services/vente.service';
import {VenteEditDialogComponent} from '../features/ventes/vente-edit-dialog/vente-edit-dialog.component';

/**
 * Ecrans de liste clients et ventes.
 *
 * Ils partagent la meme mecanique : chargement pagine, edition par boite de
 * dialogue, suppression sous confirmation. Les tests portent sur ce qui casse
 * en pratique — le rechargement apres une modification (sans lui l ecran ment
 * sur l etat des donnees) et la confirmation avant suppression.
 */
describe('ClientsComponent', () => {
  let fixture: ComponentFixture<ClientsComponent>;
  let component: ClientsComponent;
  let srv: any;
  let auth: any;
  let dialog: any;
  let apresFermeture: any;

  const clients = [
    {id: 1, nom: 'Dupont', email: 'd@test.fr'},
    {id: 2, nom: 'Martin', email: 'm@test.fr'},
  ];

  beforeEach(async () => {
    apresFermeture = of(null);
    srv = {
      getPage: jest.fn().mockReturnValue(of(clients)),
      delete: jest.fn().mockReturnValue(of(null)),
    };
    auth = {isAdmin: jest.fn().mockReturnValue(true)};
    dialog = {open: jest.fn(() => ({afterClosed: () => apresFermeture}))};

    await TestBed.configureTestingModule({
      declarations: [ClientsComponent],
      imports: [...MATERIEL],
      providers: [
        {provide: ClientService, useValue: srv},
        {provide: AuthService, useValue: auth},
        {provide: MatDialog, useValue: dialog},
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ClientsComponent);
    component = fixture.componentInstance;
  });

  describe('chargement', () => {
    it('charge la premiere page a l initialisation', () => {
      component.ngOnInit();

      expect(srv.getPage).toHaveBeenCalledWith(0, 10);
      expect(component.data.data).toEqual(clients);
    });

    it('reporte le statut admin depuis le service d authentification', () => {
      component.ngOnInit();
      expect(component.isAdmin).toBe(true);
    });

    it('un utilisateur non admin ne passe pas pour un admin', () => {
      auth.isAdmin.mockReturnValue(false);
      component.ngOnInit();
      expect(component.isAdmin).toBe(false);
    });
  });

  describe('pagination', () => {
    it('recharge avec la page et la taille demandees', () => {
      component.onPage({pageIndex: 2, pageSize: 25});

      expect(component.pageIndex).toBe(2);
      expect(component.pageSize).toBe(25);
      expect(srv.getPage).toHaveBeenCalledWith(2, 25);
    });
  });

  describe('edition', () => {
    it('ouvre la boite de dialogue avec le client cible', () => {
      component.edit(clients[0] as any);

      expect(dialog.open).toHaveBeenCalledWith(
        expect.anything(),
        expect.objectContaining({data: {client: clients[0]}}));
    });

    it('recharge la liste quand la modification est confirmee', () => {
      apresFermeture = of(clients[0]);
      component.edit(clients[0] as any);

      expect(srv.getPage).toHaveBeenCalled();
    });

    it('ne recharge pas quand la boite est fermee sans modification', () => {
      apresFermeture = of(null);
      component.edit(clients[0] as any);

      expect(srv.getPage)
        .not.toHaveBeenCalled();
    });
  });

  describe('suppression', () => {
    it('supprime et recharge apres confirmation', () => {
      window.confirm = jest.fn(() => true);

      component.remove(clients[0] as any);

      expect(srv.delete).toHaveBeenCalledWith(1);
      expect(srv.getPage).toHaveBeenCalled();
    });

    it('ne supprime rien si la confirmation est refusee', () => {
      window.confirm = jest.fn(() => false);

      component.remove(clients[0] as any);

      expect(srv.delete)
        .not.toHaveBeenCalled();
    });
  });
});

describe('VentesComponent', () => {
  let component: VentesComponent;
  let srv: any;
  let auth: any;
  let dialog: any;
  let apresFermeture: any;

  const ventes = [{id: 1, montant: 24000}, {id: 2, montant: 18000}];

  beforeEach(async () => {
    apresFermeture = of(null);
    srv = {
      getPage: jest.fn().mockReturnValue(of(ventes)),
      delete: jest.fn().mockReturnValue(of(null)),
    };
    auth = {isAdmin: jest.fn().mockReturnValue(true)};
    dialog = {open: jest.fn(() => ({afterClosed: () => apresFermeture}))};

    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      declarations: [VentesComponent],
      imports: [...MATERIEL],
      providers: [
        {provide: VenteService, useValue: srv},
        {provide: AuthService, useValue: auth},
        {provide: MatDialog, useValue: dialog},
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    component = TestBed.createComponent(VentesComponent).componentInstance;
  });

  it('charge la premiere page a l initialisation', () => {
    component.ngOnInit();

    expect(srv.getPage).toHaveBeenCalledWith(0, 10);
    expect(component.data.data).toEqual(ventes);
  });

  it('la pagination declenche un rechargement', () => {
    component.onPage({pageIndex: 1, pageSize: 50});

    expect(srv.getPage).toHaveBeenCalledWith(1, 50);
  });

  it('l edition ouvre la boite de dialogue dediee aux ventes', () => {
    component.edit(ventes[0] as any);

    expect(dialog.open).toHaveBeenCalledWith(
      VenteEditDialogComponent,
      expect.objectContaining({data: ventes[0]}));
  });

  it('une vente modifiee declenche un rechargement', () => {
    apresFermeture = of(ventes[0]);
    component.edit(ventes[0] as any);

    expect(srv.getPage).toHaveBeenCalled();
  });

  it('la suppression exige une confirmation', () => {
    window.confirm = jest.fn(() => false);

    component.remove(ventes[0] as any);

    expect(srv.delete)
      .not.toHaveBeenCalled();
  });

  it('la suppression confirmee supprime puis recharge', () => {
    window.confirm = jest.fn(() => true);

    component.remove(ventes[0] as any);

    expect(srv.delete).toHaveBeenCalledWith(1);
    expect(srv.getPage).toHaveBeenCalled();
  });
});
