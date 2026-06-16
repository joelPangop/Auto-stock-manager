export type CategorieVoiture = 'CITADINE' | 'BERLINE' | 'SUV_4X4' | 'PREMIUM';

export const CATEGORIE_LABELS: Record<CategorieVoiture, string> = {
  CITADINE: 'Citadine',
  BERLINE:  'Berline',
  SUV_4X4:  'SUV / 4x4',
  PREMIUM:  'Premium'
};
