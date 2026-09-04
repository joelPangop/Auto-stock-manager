// Doit rester aligne sur org.autostock.enums.StatutVoiture : toute valeur
// absente cote backend fait echouer la deserialisation du payload (400).
export type StatutVoiture = 'EN_STOCK' | 'RESERVEE' | 'VENDUE' | 'HORS_SERVICE';
