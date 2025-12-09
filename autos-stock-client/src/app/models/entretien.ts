export interface Entretien {
  id: number;
  idVoiture: number;
  type: string;
  typeLabel: string;
  dateEntretien: any; // ISO
  commentaire?: string;
  cout?: number;
}
