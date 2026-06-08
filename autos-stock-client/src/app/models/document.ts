import {TypeDocument} from "./enums/TypeDocument";

export interface Document {
  id: number;
  voitureId: number;
  type: TypeDocument;         // ex: "FACTURE", "ATTESTATION", ...
  description?: string;
  url?: string;
  nomFichier?: string;
  dateUpload?: string;
  createdAt?: string;
  montant?: number;
  principale?: boolean;       // photo principale du véhicule
}
