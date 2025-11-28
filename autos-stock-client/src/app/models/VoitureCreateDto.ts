import {StatutVoiture} from "./enums/StatutVoiture";

export interface VoitureCreateDto {
  marque: string;
  modele: string;
  annee: number;
  vin?: string;
  couleur?: string;
  kilometrage?: number;
  prixAchat?: number;
  prixVente?: number;
  statut?: StatutVoiture;     // par défaut: EN_STOCK
  idFournisseur?: number;
  dateEntreeStock?: string;   // 'YYYY-MM-DD'
  creerMouvementEntree?: boolean; // true => crée un mouvement "ENTREE_STOCK"
}
