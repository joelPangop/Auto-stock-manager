import api from './client'
import type { ContactDto } from '@/types'

export const contactApi = {
  envoyer: (dto: ContactDto) =>
    api.post('/public/contact', dto).then(r => r.data),
}
