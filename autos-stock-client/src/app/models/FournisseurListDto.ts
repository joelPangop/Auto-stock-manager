export interface FournisseurListDto {
  id: number;
  nom: string;
  type: 'CONCESSION' | 'PARTICULIER' | 'AUTRE';
}
