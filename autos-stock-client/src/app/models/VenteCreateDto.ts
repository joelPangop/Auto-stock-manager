import {MethodePaiement} from "./enums/MethodePaiement";

export interface VenteCreateDto {
  idVoiture: number;
  idClient: number;
  idVendeur: number;
  dateVente: string;       // ISO (ex: 2025-11-26T15:00:00Z) ou 'YYYY-MM-DD'
  prixFinal: number;
  modePaiement: MethodePaiement;
  acompteMontant?: number; // optionnel, pour un paiement initial
}

export interface VenteDto extends VenteCreateDto {
  id: number;
}
