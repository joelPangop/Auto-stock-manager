export interface Entretien {
  id: number;
  voitureId: number;
  type: string;
  typeLabel: string;
  dateEntretien: string; // ISO
  commentaire?: string;
  cout?: number;
}
