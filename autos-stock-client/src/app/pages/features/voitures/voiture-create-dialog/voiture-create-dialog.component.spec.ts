import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {of, throwError} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';
import {TranslateModule} from '@ngx-translate/core';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatDividerModule} from '@angular/material/divider';
import {MatIconModule} from '@angular/material/icon';
import {MatDialogModule} from '@angular/material/dialog';

import {VoitureCreateDialogComponent} from './voiture-create-dialog.component';
import {VoitureService} from '../../../../services/voiture.service';
import {FournisseurService} from '../../../../services/fournisseur.service';
import {MarqueService} from '../../../../services/marque.service';
import {ModeleService} from '../../../../services/modele.service';

/**
 * Creation d'une voiture. Le formulaire est envoye tel quel au backend : toute
 * divergence avec le contrat Java (valeur d'enum inconnue, contrainte NOT NULL
 * ou UNIQUE non respectee) revient sous la forme d'un « Erreur lors de
 * l'enregistrement » qui ne dit rien de la cause.
 */
describe('VoitureCreateDialogComponent', () => {
  let fixture: ComponentFixture<VoitureCreateDialogComponent>;
  let component: VoitureCreateDialogComponent;
  let voitures: any;
  let dialogRef: any;

  const formulaireValide = () => ({
    idMarque: 1,
    idModele: 2,
    annee: 2020,
    vin: '1HGCM82633A004352',
    couleur: 'Noir',
    kilometrage: 1000,
    prixAchat: 5000,
    prixVente: 8000,
    statut: 'EN_STOCK',
    idFournisseur: null,
  });

  beforeEach(async () => {
    voitures = {create: jest.fn().mockReturnValue(of({id: 42}))};
    dialogRef = {close: jest.fn()};

    await TestBed.configureTestingModule({
      declarations: [VoitureCreateDialogComponent],
      // Les vrais modules Material sont necessaires : sans eux les mat-select
      // n'ont pas de ControlValueAccessor et le template ne se lie plus au
      // formulaire. C'est aussi ce qui fait de ce test une verification du
      // template, pas seulement de la classe.
      imports: [
        ReactiveFormsModule, NoopAnimationsModule, TranslateModule.forRoot(),
        MatFormFieldModule, MatInputModule, MatSelectModule, MatCheckboxModule,
        MatDividerModule, MatIconModule, MatDialogModule,
      ],
      providers: [
        {provide: VoitureService, useValue: voitures},
        {provide: FournisseurService, useValue: {listAll: () => of([])}},
        {provide: MarqueService, useValue: {list: () => of([])}},
        {provide: ModeleService, useValue: {listByMarque: () => of([])}},
        {provide: MatDialogRef, useValue: dialogRef},
        {provide: MatDialog, useValue: {open: jest.fn()}},
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(VoitureCreateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('contrat avec le backend', () => {
    it('ne propose que des statuts connus de org.autostock.enums.StatutVoiture', () => {
      // DISPONIBLE n'existe pas cote Java : le choisir faisait echouer la
      // deserialisation du corps de la requete, donc toute la creation.
      expect(component.statuts).toEqual(['EN_STOCK', 'RESERVEE', 'VENDUE', 'HORS_SERVICE']);
      expect(component.statuts).not.toContain('DISPONIBLE');
    });

    it('exige un VIN, la colonne etant NOT NULL et UNIQUE', () => {
      const vin = component.form.get('vin')!;

      expect(vin.value).toBe('');
      expect(vin.hasError('required')).toBe(true);

      vin.setValue('1HGCM82633A004352');
      expect(vin.hasError('required')).toBe(false);
    });
  });

  describe('enregistrement', () => {
    it('ferme le dialogue avec la voiture creee', () => {
      component.form.patchValue(formulaireValide());

      component.save();

      expect(voitures.create).toHaveBeenCalled();
      expect(dialogRef.close).toHaveBeenCalledWith({id: 42});
      expect(component.apiError).toBeNull();
    });

    it('n envoie rien tant que le formulaire est incomplet', () => {
      component.save();

      expect(voitures.create).not.toHaveBeenCalled();
    });
  });

  describe('echec cote API', () => {
    const echouerAvec = (body: any) => {
      voitures.create.mockReturnValue(
        throwError(new HttpErrorResponse({status: 409, error: body}))
      );
      component.form.patchValue(formulaireValide());
      component.save();
    };

    it('affiche le message du backend plutot qu une erreur generique', () => {
      echouerAvec({message: 'Ce VIN est deja utilise par une autre voiture.'});

      expect(component.apiError).toBe('Ce VIN est deja utilise par une autre voiture.');
    });

    it('laisse le bouton Enregistrer utilisable pour reessayer', () => {
      // L'ancien code posait une erreur sur le formulaire lui-meme, ce qui le
      // rendait invalide en permanence et desactivait le bouton.
      echouerAvec({message: 'Ce VIN est deja utilise par une autre voiture.'});

      expect(component.loading).toBe(false);
      expect(component.form.valid).toBe(true);
      expect(component.form.errors).toBeNull();
    });

    it('retombe sur le message generique si le backend n en fournit pas', () => {
      echouerAvec(null);

      expect(component.apiError).toBeNull();
    });
  });
});
