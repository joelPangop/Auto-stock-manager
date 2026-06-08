import api from './client'
import type { Reservation, ReservationCreateDto } from '@/types'

export const reservationApi = {
  create: (dto: ReservationCreateDto) =>
    api.post<Reservation>('/client/reservations', dto).then(r => r.data),

  mes: () =>
    api.get<Reservation[]>('/client/reservations').then(r => r.data),

  annuler: (id: number) =>
    api.delete(`/client/reservations/${id}`).then(r => r.data),
}
