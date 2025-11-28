export interface Mouvement {
  id: number;
  voitureId: number;
  type: 'ENTREE' | 'SORTIE' | 'AJUSTEMENT';
  dateMouvement: string; // ISO
  commentaire?: string;
}
