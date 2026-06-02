export interface Entretien {
  id: number;
  idVoiture: number;
  voitureLabel: string;
  type: string;
  typeLabel: string;
  dateEntretien: any; // ISO
  commentaire?: string;
  cout?: number;
}
