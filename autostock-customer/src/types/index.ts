// ── Véhicule (catalogue public) ──────────────────────────────────
export interface VehiculePublic {
  id: number
  marque: string
  modele: string
  annee: number
  couleur?: string
  kilometrage?: number
  prixVente: number
  statut: 'EN_STOCK' | 'RESERVEE' | 'VENDUE' | 'HORS_SERVICE'
  /** ID de la photo principale (aperçu liste catalogue) */
  photoId?: number
}

export interface VehiculePublicDetail extends VehiculePublic {
  vin?: string
  /** IDs de toutes les photos du véhicule */
  photoIds: number[]
  description?: string
}

export interface FiltresCatalogue {
  marque?: string
  anneeMin?: number
  prixMax?: number
  search?: string
  page?: number
  size?: number
}

// ── Auth client ───────────────────────────────────────────────────
export interface ClientRegisterDto {
  nom: string
  email: string
  motDePasse: string
  telephone?: string
}

export interface ClientLoginDto {
  email: string
  motDePasse: string
}

export interface AuthResponse {
  token: string
  client: ClientProfil
}

export interface ClientProfil {
  id: number
  nom: string
  email: string
  telephone?: string
}

// ── Réservation ───────────────────────────────────────────────────
export interface ReservationCreateDto {
  voitureId: number
  message?: string
  dateVisite?: string  // ISO date YYYY-MM-DD
}

export interface Reservation {
  id: number
  voitureId: number
  voitureLabel: string
  statut: 'EN_ATTENTE' | 'CONFIRMEE' | 'ANNULEE'
  dateVisite?: string
  message?: string
  createdAt: string
}

// ── Contact ───────────────────────────────────────────────────────
export interface ContactDto {
  nom: string
  email: string
  telephone?: string
  sujet: string
  message: string
}

// ── Pagination (Spring Page) ──────────────────────────────────────
export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
