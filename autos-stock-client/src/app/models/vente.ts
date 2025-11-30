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
  modePaiement: string; // ton enum côté backend
  statut?: string;
  marque: string;
}
