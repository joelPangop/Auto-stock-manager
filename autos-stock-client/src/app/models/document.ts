export interface Document {
  id: number;
  voitureId: number;
  type: string;         // ex: "FACTURE", "ATTESTATION", ...
  description?: string;
  url?: string;         // si exposé
  createdAt?: string;
}
