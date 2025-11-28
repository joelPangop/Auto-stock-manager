export interface Vente {
  id: number;
  voitureId: number;
  clientId: number;
  dateVente: string;   // ISO
  prixVente: number;
  modePaiement: string; // ton enum côté backend
  statut?: string;
}
