import {DepenseCreateDto} from "./DepenseCreateDto";

export interface DepenseDto {
  id: number;
  voitureId: number;
  entretienId?: number | null;
  documentId?: number | null;

  fournisseurId?: number | null;
  fournisseurNom?: string | null;

  categorie: string;
  montant: number;
  dateDepense: string; // YYYY-MM-DD

  description?: string | null;
  kilometrage?: number | null;
}
