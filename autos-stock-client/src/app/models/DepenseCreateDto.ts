export interface DepenseCreateDto {
  categorie: string;
  montant: number;
  dateDepense: string;
  description?: string | null;
  kilometrage?: number | null;

  fournisseurId?: number | null;
  documentId?: number | null;
  entretienId?: number | null;
}
