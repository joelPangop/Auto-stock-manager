export interface FournisseurCreateDto {
  nom: string;
  type: 'CONCESSION' | 'PARTICULIER' | 'AUTRE';
  adresse?: string;
  telephone?: string;
}

