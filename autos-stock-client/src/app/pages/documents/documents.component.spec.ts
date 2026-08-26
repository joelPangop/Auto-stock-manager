import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateModule} from '@ngx-translate/core';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatExpansionModule} from '@angular/material/expansion';
import {of, throwError} from 'rxjs';

import {DocumentsComponent} from './documents.component';
import {DocumentService} from '../../services/document.service';

/**
 * Ecran documents : regroupement par type en dossiers, recherche, suppression.
 *
 * Les photos sont volontairement exclues de cet ecran — elles appartiennent a
 * la fiche vehicule. Le test correspondant empeche qu une modification du
 * regroupement les fasse reapparaitre ici et noie les pieces administratives.
 */
describe('DocumentsComponent', () => {
  let fixture: ComponentFixture<DocumentsComponent>;
  let component: DocumentsComponent;
  let srv: any;
  let router: any;

  const docs = [
    {id: 1, type: 'FACTURE', typeLabel: 'Facture', nomFichier: 'facture-honda.pdf',
     voitureLabel: 'Honda Civic', vendeurNom: 'Diane', clientNom: 'Marc', description: 'Achat'},
    {id: 2, type: 'FACTURE', typeLabel: 'Facture', nomFichier: 'facture-toyota.pdf',
     voitureLabel: 'Toyota Yaris', vendeurNom: 'Eric', clientNom: 'Julie', description: ''},
    {id: 3, type: 'CARFAX', typeLabel: 'Carfax', nomFichier: 'rapport.pdf',
     voitureLabel: 'Honda Civic', vendeurNom: 'Diane', clientNom: '', description: 'Historique'},
    {id: 4, type: 'PHOTO', typeLabel: 'Photo', nomFichier: 'avant.jpg',
     voitureLabel: 'Honda Civic', vendeurNom: 'Diane', clientNom: '', description: ''},
  ];

  beforeEach(async () => {
    srv = {
      listAll: jest.fn().mockReturnValue(of(docs)),
      delete: jest.fn().mockReturnValue(of(null)),
      download: jest.fn().mockReturnValue(of(new Blob(['pdf']))),
    };
    router = {navigate: jest.fn()};

    await TestBed.configureTestingModule({
      declarations: [DocumentsComponent],
      imports: [
        FormsModule, NoopAnimationsModule, TranslateModule.forRoot(), MatTableModule,
        MatIconModule, MatButtonModule, MatCardModule, MatTooltipModule,
        MatFormFieldModule, MatInputModule, MatExpansionModule,
      ],
      providers: [
        {provide: DocumentService, useValue: srv},
        {provide: Router, useValue: router},
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(DocumentsComponent);
    component = fixture.componentInstance;

    (window as any).URL.createObjectURL = jest.fn(() => 'blob:faux');
    (window as any).URL.revokeObjectURL = jest.fn();
    HTMLAnchorElement.prototype.click = jest.fn();
  });

  describe('chargement et regroupement', () => {
    it('exclut les photos, qui relevent de la fiche vehicule', () => {
      component.ngOnInit();

      expect(component.allDocs.map(d => d.id)).not.toContain(4);
      expect(component.allDocs).toHaveLength(3);
    });

    it('regroupe les documents par type en dossiers', () => {
      component.ngOnInit();

      expect(component.folders.map(f => f.type)).toEqual(['FACTURE', 'CARFAX']);
      expect(component.folders[0].count).toBe(2);
    });

    it('respecte l ordre metier des types, pas l ordre d arrivee', () => {
      srv.listAll.mockReturnValue(of([
        {id: 1, type: 'AUTRE', nomFichier: 'x.pdf'},
        {id: 2, type: 'FACTURE', nomFichier: 'y.pdf'},
      ]));

      component.ngOnInit();

      expect(component.folders.map(f => f.type)).toEqual(['FACTURE', 'AUTRE']);
    });

    it('les dossiers sont fermes au depart', () => {
      component.ngOnInit();

      expect(component.folders.every(f => !f.open)).toBe(true);
    });

    it('une liste vide ne provoque pas d erreur', () => {
      srv.listAll.mockReturnValue(of([]));

      component.ngOnInit();

      expect(component.folders).toEqual([]);
      expect(component.error).toBe('');
    });

    it('une reponse nulle est traitee comme une liste vide', () => {
      srv.listAll.mockReturnValue(of(null));

      expect(() => component.ngOnInit()).not.toThrow();
      expect(component.allDocs).toEqual([]);
    });

    it('une erreur serveur est affichee avec son code', () => {
      srv.listAll.mockReturnValue(throwError({status: 503, message: 'Service indisponible'}));

      component.ngOnInit();

      expect(component.error).toContain('503');
    });
  });

  /** flatMap demande la lib ES2019, absente du tsconfig du projet. */
  const idsAffiches = (): number[] =>
    component.folders.reduce<number[]>(
      (acc, f) => acc.concat(f.documents.map(d => d.id)), []);

  describe('recherche', () => {
    beforeEach(() => component.ngOnInit());

    it('filtre sur le nom de fichier', () => {
      component.onSearch('honda');

      const ids = idsAffiches();
      expect(ids).toContain(1);
      expect(ids).not.toContain(2);
    });

    it('filtre aussi sur le vehicule, le vendeur et le client', () => {
      component.onSearch('julie');

      const ids = idsAffiches();
      expect(ids).toEqual([2]);
    });

    it('ignore la casse', () => {
      component.onSearch('DIANE');

      const ids = idsAffiches();
      expect(ids).toEqual(expect.arrayContaining([1, 3]));
    });

    it('ouvre les dossiers pour montrer les resultats', () => {
      component.onSearch('honda');

      expect(component.folders.every(f => f.open)).toBe(true);
    });

    it('une recherche vide restaure la totalite des documents', () => {
      component.onSearch('honda');
      component.onSearch('   ');

      const ids = idsAffiches();
      expect(ids).toHaveLength(3);
    });

    it('une recherche sans resultat ne laisse aucun dossier', () => {
      component.onSearch('introuvable-xyz');

      expect(component.folders).toEqual([]);
    });
  });

  describe('suppression', () => {
    beforeEach(() => component.ngOnInit());

    it('demande confirmation avant de supprimer', () => {
      window.confirm = jest.fn(() => false);

      component.delete(component.folders[0].documents[0], component.folders[0]);

      expect(srv.delete).not.toHaveBeenCalled();
    });

    it('retire le document du dossier et du cache local', () => {
      window.confirm = jest.fn(() => true);
      const dossier = component.folders[0];
      const doc = dossier.documents[0];

      component.delete(doc, dossier);

      expect(srv.delete).toHaveBeenCalledWith(doc.id);
      expect(dossier.documents.map(d => d.id)).not.toContain(doc.id);
      // Le cache local doit suivre : sinon une recherche ulterieure, qui repart
      // de allDocs, ferait reapparaitre un document supprime.
      expect(component.allDocs.map(d => d.id)).not.toContain(doc.id);
    });

    it('met le compteur du dossier a jour', () => {
      window.confirm = jest.fn(() => true);
      const dossier = component.folders[0];

      component.delete(dossier.documents[0], dossier);

      expect(dossier.count).toBe(1);
    });

    it('un dossier vide disparait de la liste', () => {
      window.confirm = jest.fn(() => true);
      const carfax = component.folders.find(f => f.type === 'CARFAX')!;

      component.delete(carfax.documents[0], carfax);

      expect(component.folders.map(f => f.type)).not.toContain('CARFAX');
    });
  });

  describe('navigation et telechargement', () => {
    beforeEach(() => component.ngOnInit());

    it('ouvre la fiche du vehicule', () => {
      component.goToVoiture(42);

      expect(router.navigate).toHaveBeenCalledWith(['/voitures', 42]);
    });

    it('ne navigue pas sans identifiant de vehicule', () => {
      component.goToVoiture(undefined);

      expect(router.navigate).not.toHaveBeenCalled();
    });

    it('telecharge le document demande', () => {
      component.download(component.folders[0].documents[0]);

      expect(srv.download).toHaveBeenCalledWith(1);
    });
  });

  describe('reconnaissance du type de fichier', () => {
    it.each(['photo.jpg', 'photo.JPEG', 'image.png', 'anim.gif', 'moderne.webp'])(
      '%s est reconnu comme une image', nom => {
        expect(component.isImage({nomFichier: nom} as any)).toBe(true);
      });

    it('un PDF n est pas une image', () => {
      expect(component.isImage({nomFichier: 'doc.pdf'} as any)).toBe(false);
      expect(component.isPdf({nomFichier: 'doc.pdf'} as any)).toBe(true);
    });

    it('un nom de fichier absent ne fait pas planter la detection', () => {
      expect(component.isImage({} as any)).toBe(false);
      expect(component.isPdf({} as any)).toBe(false);
    });
  });
});
