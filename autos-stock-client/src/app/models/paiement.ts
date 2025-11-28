export interface Paiement {
  id: number;
  venteId: number;
  montant: number;
  datePaiement: string;   // ISO
  methode: string;        // ex: "CASH", "CARTE", ...
  reference?: string;
}
