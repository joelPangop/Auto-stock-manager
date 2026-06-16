export interface Mouvement {
  id: number;
  voitureId?: number;
  voitureLabel?: string;
  voitureStatut?: string;
  type: string;
  typeLabel?: string;
  dateMouvement: string;
  commentaire?: string;
}
