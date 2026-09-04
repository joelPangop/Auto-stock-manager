// Noms de constantes de org.autostock.enums.TypeDocument : c'est ce que
// l'API renvoie et ce que TypeDocument.valueOf() attend a l'ecriture.
export type TypeDocument =
  | 'FACTURE'
  | 'CARFAX'
  | 'PHOTO'
  | 'IMMATRICULATION'
  | 'INSPECTION'
  | 'CONTRAT'
  | 'RECU_PAIEMENT'
  | 'AUTRE';
