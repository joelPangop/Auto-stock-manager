export interface DocumentRichDto {
  id: number;
  type: string;
  typeLabel: string;
  nomFichier?: string;
  description?: string;
  montant?: number;
  dateUpload?: string;
  principale?: boolean;
  urlFichier?: string;

  // Voiture
  voitureId?: number;
  voitureLabel?: string;
  couleur?: string;
  vin?: string;

  // Vendeur
  vendeurNom?: string;
  vendeurEmail?: string;

  // Client
  clientNom?: string;
  clientTelephone?: string;
}
