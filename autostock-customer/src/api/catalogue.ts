import api from './client'
import type { FiltresCatalogue, Page, VehiculePublic, VehiculePublicDetail } from '@/types'

export const catalogueApi = {
  list: (filtres?: FiltresCatalogue) =>
    api.get<Page<VehiculePublic>>('/public/vehicules', { params: filtres })
      .then(r => r.data),

  getById: (id: number) =>
    api.get<VehiculePublicDetail>(`/public/vehicules/${id}`)
      .then(r => r.data),

  getMarques: () =>
    api.get<string[]>('/public/marques').then(r => r.data),

  /** Retourne l'URL directe pour afficher une photo (pas de blob, juste l'URL). */
  getPhotoUrl: (documentId: number) =>
    `/api/public/vehicules/photo/${documentId}`,
}
