
// @ts-ignore
import {provideAnimations} from '@angular/platform-browser/animations';
// @ts-ignore
import {importProvidersFrom} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

export const MATERIAL_IMPORTS = [
  MatButtonModule, MatInputModule, MatFormFieldModule, MatCardModule,
  MatIconModule, MatSnackBarModule, MatProgressSpinnerModule
];

export const provideMaterial = () => [
  provideAnimations(),
  importProvidersFrom(...MATERIAL_IMPORTS)
];
