export interface Vente {
  id: number;
  voitureId: number;
  clientId: number;
  dateVente: string;   // ISO
  prixVente: number;
  nomClient: number;
  nomVendeur: number;
  prixFinal: number;
  modele: number;
  statut?: string;
  marque: string;
}
