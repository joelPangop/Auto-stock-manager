import {StatutVoiture} from "./enums/StatutVoiture";

export interface VoitureListDto {
  id: number;
  marque: string;
  modele: string;
  annee: number;
  prixDemande: number;
  prixVente: number;
  statut: StatutVoiture;
  needsRemark: boolean;
}
