import {StatutVoiture} from "./enums/StatutVoiture";
import {CategorieVoiture} from "./enums/CategorieVoiture";

export interface VoitureDetailDto {
  owner: number;
  id: number;
  idMarque: number;
  idModele: number;
  annee: number;
  prixAchat: number;
  /** Prix affiche sur la vitrine, saisi sur la fiche. */
  prixDemande: number;
  /** Montant reellement encaisse, renseigne par la vente. */
  prixVente: number;
  depenseDivers: number;
  idFournisseur: number;
  vin?: string;
  couleur?: string;
  kilometrage?: number;
  statut: StatutVoiture;
  categorie?: CategorieVoiture;
  categorieLabel?: string;
  createdAt?: string;
  updatedAt?: string;
  dateEntreeStock?: string;
  needsRemark: boolean;
  description?: string;
}
