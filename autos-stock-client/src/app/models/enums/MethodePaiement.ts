// Valeurs attendues par le backend : org.autostock.enums.MethodePaiement expose
// son champ `value` (et non le nom de la constante) via @JsonValue, et
// PaiementServiceImpl les resout avec fromValue(). Envoyer autre chose leve une
// IllegalArgumentException cote serveur, soit un 400.
export type MethodePaiement = 'CASH' | 'CARD' | 'CHEQUE' | 'VIREMENT' | 'FINANCEMENT';

export const METHODE_PAIEMENT_LABELS: Record<MethodePaiement, string> = {
  CASH:        'Comptant',
  CARD:        'Carte bancaire',
  CHEQUE:      'Chèque',
  VIREMENT:    'Virement bancaire',
  FINANCEMENT: 'Financement'
};

export const METHODES_PAIEMENT: { value: MethodePaiement; label: string }[] =
  (Object.keys(METHODE_PAIEMENT_LABELS) as MethodePaiement[])
    .map(value => ({value, label: METHODE_PAIEMENT_LABELS[value]}));
