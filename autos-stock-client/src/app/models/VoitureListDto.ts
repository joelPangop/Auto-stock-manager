import {StatutVoiture} from "./enums/StatutVoiture";

export interface VoitureListDto {
  id: number;
  marque: string;
  modele: string;
  annee: number;
  prixVente: number;
  statut: StatutVoiture;
}
