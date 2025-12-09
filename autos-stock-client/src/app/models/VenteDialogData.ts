export interface VenteDialogData {
  idVoiture: number;
  prixSuggere?: number; // ex: prixVente actuel
  vendeurCourantId?: number; // si tu stockes l'utilisateur connecté
}
