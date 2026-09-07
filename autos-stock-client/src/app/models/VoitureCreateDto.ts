import {StatutVoiture} from "./enums/StatutVoiture";
import {CategorieVoiture} from "./enums/CategorieVoiture";

export interface VoitureCreateDto {
  marque: string;
  modele: string;
  annee: number;
  vin?: string;
  couleur?: string;
  kilometrage?: number;
  prixAchat?: number;
  /** Prix affiche sur la vitrine. Le prix de vente reel vient de la vente. */
  prixDemande?: number;
  statut?: StatutVoiture;
  categorie?: CategorieVoiture;
  idFournisseur?: number;
  dateEntreeStock?: string;   // 'YYYY-MM-DD'
  creerMouvementEntree?: boolean; // true => crée un mouvement "ENTREE_STOCK"
}
