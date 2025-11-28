import {StatutVoiture} from "./enums/StatutVoiture";

export interface VoitureDetailDto {
  id: number;
  idMarque: number;
  idModele: number;
  annee: number;
  prixAchat: number;
  prixVente: number;
  idFournisseur: number;
  vin?: string;
  couleur?: string;
  kilometrage?: number;
  statut: StatutVoiture;
  createdAt?: string;
  updatedAt?: string;
}
