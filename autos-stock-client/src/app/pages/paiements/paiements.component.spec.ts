import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateModule} from '@ngx-translate/core';
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {of} from 'rxjs';

import {PaiementsComponent} from './paiements.component';
import {PaiementService} from '../../services/paiement.service';
import {AuthService} from '../../services/auth.service';
import {PaiementViewDialogComponent} from '../features/paiement-view-dialog/paiement-view-dialog.component';

/**
 * Ecran paiements — pilote par des flux reactifs plutot que par des appels
 * imperatifs. Chaque changement (page, taille, tri, filtre, recherche) relance
 * la requete via combineLatest.
 *
 * Deux comportements meritent d'etre figes : la recherche est debouncee a
 * 250 ms (sans quoi chaque frappe declenche un appel serveur), et tout
 * changement de critere doit ramener a la premiere page — rester en page 5
 * apres un filtrage affiche un tableau vide et donne l'illusion d'un bug.
 */
describe('PaiementsComponent', () => {
  let fixture: ComponentFixture<PaiementsComponent>;
  let component: PaiementsComponent;
  let srv: any;
  let auth: any;
  let dialog: any;

  const paiements = [
    {id: 1, voitureLabel: 'Honda Civic', methode: 'CASH', reference: 'REF-001',
     montant: 10000, datePaiement: '2026-08-01', venteId: 7},
    {id: 2, voitureLabel: 'Toyota Yaris', methode: 'CARD', reference: 'REF-002',
     montant: 5000, datePaiement: '2026-08-15', venteId: 8},
  ];

  const page = (items: any[] = paiements) => ({items, total: items.length, page: 0, size: 10});

  beforeEach(async () => {
    srv = {getPage: jest.fn().mockReturnValue(of(page()))};
    auth = {isAdmin: jest.fn().mockReturnValue(true)};
    dialog = {open: jest.fn(() => ({afterClosed: () => of(null)}))};

    await TestBed.configureTestingModule({
      declarations: [PaiementsComponent],
      imports: [
        FormsModule, NoopAnimationsModule, TranslateModule.forRoot(), MatTableModule,
        MatPaginatorModule, MatSortModule, MatIconModule, MatButtonModule, MatCardModule,
        MatTooltipModule, MatFormFieldModule, MatInputModule, MatSlideToggleModule,
      ],
      providers: [
        {provide: PaiementService, useValue: srv},
        {provide: AuthService, useValue: auth},
        {provide: MatDialog, useValue: dialog},
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(PaiementsComponent);
    component = fixture.componentInstance;
  });

  /** Le flux n'emet qu'une fois abonne : combineLatest est froid. */
  const abonner = (recepteur: (vm: any) => void) =>
    component.pageVm$.subscribe(recepteur);

  describe('initialisation', () => {
    it('reporte le statut admin', () => {
      component.ngOnInit();
      expect(component.isAdmin).toBe(true);
    });

    it('un non-admin ne passe pas pour un admin', () => {
      auth.isAdmin.mockReturnValue(false);
      component.ngOnInit();
      expect(component.isAdmin).toBe(false);
    });

    it('charge la premiere page avec le tri par defaut', fakeAsync(() => {
      abonner(() => {});
      tick(250);

      expect(srv.getPage).toHaveBeenCalledWith(0, 10, 'datePaiement,desc', false);
    }));
  });

  describe('pagination', () => {
    it('un changement de page relance la requete', fakeAsync(() => {
      abonner(() => {});
      tick(250);
      srv.getPage.mockClear();

      component.onPage({pageIndex: 3, pageSize: 50} as any);
      tick(250);

      expect(srv.getPage).toHaveBeenCalledWith(3, 50, 'datePaiement,desc', false);
    }));
  });

  describe('tri', () => {
    it('transmet la colonne et le sens choisis', fakeAsync(() => {
      abonner(() => {});
      tick(250);
      srv.getPage.mockClear();

      component.onSort({active: 'montant', direction: 'asc'} as any);
      tick(250);

      expect(srv.getPage).toHaveBeenCalledWith(0, 10, 'montant,asc', false);
    }));

    it('retombe sur des valeurs sures quand le tri est vide', fakeAsync(() => {
      abonner(() => {});
      tick(250);
      srv.getPage.mockClear();

      component.onSort({active: '', direction: ''} as any);
      tick(250);

      expect(srv.getPage).toHaveBeenCalledWith(0, 10, 'datePaiement,desc', false);
    }));

    it('un tri ramene a la premiere page', fakeAsync(() => {
      abonner(() => {});
      component.onPage({pageIndex: 4, pageSize: 10} as any);
      tick(250);
      srv.getPage.mockClear();

      component.onSort({active: 'montant', direction: 'desc'} as any);
      tick(250);

      expect(srv.getPage).toHaveBeenCalledWith(0, 10, 'montant,desc', false);
    }));
  });

  describe('filtre « mes paiements »', () => {
    it('transmet le filtre au serveur', fakeAsync(() => {
      abonner(() => {});
      tick(250);
      srv.getPage.mockClear();

      component.toggleOnlyMine(true);
      tick(250);

      expect(component.onlyMine).toBe(true);
      expect(srv.getPage).toHaveBeenCalledWith(0, 10, 'datePaiement,desc', true);
    }));

    it('ramene a la premiere page', fakeAsync(() => {
      abonner(() => {});
      component.onPage({pageIndex: 6, pageSize: 10} as any);
      tick(250);
      srv.getPage.mockClear();

      component.toggleOnlyMine(true);
      tick(250);

      // toggleOnlyMine emet sur onlyMine$ PUIS sur page$. combineLatest reagit a
      // chacune : une premiere requete part avec l ancienne page (6), une
      // seconde avec la bonne (0). L etat final est correct, mais la premiere
      // requete est perdue. Comportement documente, pas valide comme souhaitable.
      const dernierAppel = srv.getPage.mock.calls[srv.getPage.mock.calls.length - 1];
      expect(dernierAppel[0]).toBe(0);
      expect(dernierAppel[3]).toBe(true);
    }));
  });

  describe('recherche', () => {
    it('filtre localement sur le libelle du vehicule', fakeAsync(() => {
      let vm: any = null;
      abonner(v => (vm = v));
      tick(250);

      component.onSearch('honda');
      tick(250);

      expect(vm.items.map((p: any) => p.id)).toEqual([1]);
    }));

    it('filtre aussi sur la methode, la reference et le montant', fakeAsync(() => {
      let vm: any = null;
      abonner(v => (vm = v));
      tick(250);

      component.onSearch('REF-002');
      tick(250);
      expect(vm.items.map((p: any) => p.id)).toEqual([2]);

      component.onSearch('5000');
      tick(250);
      expect(vm.items.map((p: any) => p.id)).toEqual([2]);
    }));

    it('ignore la casse', fakeAsync(() => {
      let vm: any = null;
      abonner(v => (vm = v));
      tick(250);

      component.onSearch('HONDA');
      tick(250);

      expect(vm.items.map((p: any) => p.id)).toEqual([1]);
    }));

    it('une recherche vide rend la liste complete', fakeAsync(() => {
      let vm: any = null;
      abonner(v => (vm = v));
      tick(250);

      component.onSearch('honda');
      tick(250);
      component.onSearch('');
      tick(250);

      expect(vm.items).toHaveLength(2);
    }));

    it('une recherche sans resultat renvoie une liste vide, pas une erreur', fakeAsync(() => {
      let vm: any = null;
      abonner(v => (vm = v));
      tick(250);

      component.onSearch('introuvable-xyz');
      tick(250);

      expect(vm.items).toEqual([]);
    }));

    it('le debounce ne protege pas le serveur : une requete part a chaque frappe', fakeAsync(() => {
      abonner(() => {});
      tick(250);
      srv.getPage.mockClear();

      component.onSearch('h');
      tick(50);
      component.onSearch('ho');
      tick(50);
      component.onSearch('hon');
      tick(250);

      // Le debounceTime(250) ne porte que sur search$. Or onSearch() emet aussi
      // sur page$, qui n est pas debounce : chaque frappe relance donc une
      // requete serveur, plus une derniere pour la recherche debouncee.
      // Le debounce donne une fausse impression de protection.
      // Correctif possible : distinctUntilChanged() sur page$.
      expect(srv.getPage.mock.calls.length).toBe(4);
    }));

    it('une page sans items ne fait pas planter le filtrage', fakeAsync(() => {
      srv.getPage.mockReturnValue(of({items: null, total: 0, page: 0, size: 10}));

      let vm: any = null;
      abonner(v => (vm = v));
      tick(250);

      component.onSearch('honda');
      tick(250);

      expect(vm.items).toEqual([]);
    }));
  });

  describe('consultation d un paiement', () => {
    it('ouvre la boite de dialogue avec l identifiant du paiement', () => {
      component.openView(42);

      expect(dialog.open).toHaveBeenCalledWith(
        PaiementViewDialogComponent,
        expect.objectContaining({data: {paiementId: 42}}));
    });
  });

  describe('cycle de vie', () => {
    it('ngOnDestroy libere le sujet de destruction sans lever', () => {
      component.ngOnInit();
      expect(() => component.ngOnDestroy()).not.toThrow();
    });
  });
});
