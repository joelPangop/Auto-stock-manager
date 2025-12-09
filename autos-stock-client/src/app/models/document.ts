import {TypeDocument} from "./enums/TypeDocument";

export interface Document {
  id: number;
  voitureId: number;
  type: TypeDocument;         // ex: "FACTURE", "ATTESTATION", ...
  description?: string;
  url?: string;         // si exposé
  nomFichier?: string;         // si exposé
  createdAt?: string;
}
