import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {HttpResponse, HttpHeaders} from '@angular/common/http';
import {of, throwError} from 'rxjs';

import {MigrationComponent} from './migration.component';
import {BackupService} from '../../services/backup.service';

/**
 * Page de migration / export, reservee au super admin.
 *
 * L'essentiel porte sur la lisibilite des echecs : un export peut echouer pour
 * des raisons tres differentes (quota, role, delai de proxy depasse) et les
 * confondre sous un « erreur inconnue » ferait perdre du temps a chaque fois.
 */
describe('MigrationComponent', () => {
  let fixture: ComponentFixture<MigrationComponent>;
  let component: MigrationComponent;
  let api: any;
  let snack: any;

  const reponseZip = (nomFichier?: string, taille = 1024) => {
    const headers = nomFichier
      ? new HttpHeaders({'Content-Disposition': `attachment; filename="${nomFichier}"`})
      : new HttpHeaders();
    return new HttpResponse({body: new Blob(['x'.repeat(taille)]), headers, status: 200});
  };

  beforeEach(async () => {
    api = {export: jest.fn().mockReturnValue(of(reponseZip('export.zip')))};
    snack = {open: jest.fn()};

    await TestBed.configureTestingModule({
      declarations: [MigrationComponent],
      imports: [FormsModule],
      providers: [
        {provide: BackupService, useValue: api},
        {provide: MatSnackBar, useValue: snack},
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(MigrationComponent);
    component = fixture.componentInstance;

    // jsdom n'implemente pas l'API des URL d'objet ni le declenchement de
    // telechargement : on les bouchonne pour pouvoir observer le reste.
    (window as any).URL.createObjectURL = jest.fn(() => 'blob:faux');
    (window as any).URL.revokeObjectURL = jest.fn();
    HTMLAnchorElement.prototype.click = jest.fn();
  });

  describe('options d export', () => {
    it('inclut uploads et S3 par defaut', () => {
      expect(component.includeUploads).toBe(true);
      expect(component.includeS3).toBe(true);
    });

    it('transmet les options telles que cochees', () => {
      component.includeUploads = false;
      component.includeS3 = true;
      component.export();

      expect(api.export).toHaveBeenCalledWith(false, true);
    });
  });

  describe('export reussi', () => {
    it('retient le nom de fichier annonce par le serveur', () => {
      api.export.mockReturnValue(of(reponseZip('autostock-export-20260825.zip')));
      component.export();

      expect(component.lastFile).toBe('autostock-export-20260825.zip');
      expect(component.errorMsg).toBeNull();
    });

    it('retombe sur un nom par defaut si l en-tete est absent', () => {
      api.export.mockReturnValue(of(reponseZip(undefined)));
      component.export();

      expect(component.lastFile).toBe('autostock-export.zip');
    });

    it('affiche une taille lisible', () => {
      api.export.mockReturnValue(of(reponseZip('e.zip', 2048)));
      component.export();

      expect(component.lastSize).toContain('Ko');
    });

    it('libere le bouton une fois termine', () => {
      component.export();
      expect(component.running).toBe(false);
    });

    it('signale une archive vide au lieu de la presenter comme un succes', () => {
      api.export.mockReturnValue(of(new HttpResponse({body: new Blob([]), status: 200})));
      component.export();

      expect(component.errorMsg).toContain('vide');
      expect(component.lastFile).toBeNull();
    });
  });

  describe('messages d erreur distincts', () => {
    const casDErreur: Array<[number, string]> = [
      [429, 'moins de 5 minutes'],
      [403, 'super administrateur'],
      [401, 'Session expirée'],
      [0, 'proxy'],
    ];

    it.each(casDErreur)('code %i donne un message specifique', (status, extrait) => {
      api.export.mockReturnValue(throwError({status}));
      component.export();

      expect(component.errorMsg).toContain(extrait);
      expect(component.running).toBe(false);
    });

    it('un code inattendu reste explicite', () => {
      api.export.mockReturnValue(throwError({status: 500}));
      component.export();

      expect(component.errorMsg).toContain('500');
    });
  });

  describe('protection contre le double clic', () => {
    it('ignore un second appel tant que l export est en cours', () => {
      component.running = true;
      component.export();

      expect(api.export)
        .not.toHaveBeenCalled();
    });
  });
});
