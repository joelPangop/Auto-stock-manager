import api from './client'
import type { AuthResponse, ClientLoginDto, ClientProfil, ClientRegisterDto } from '@/types'

export const authApi = {
  register: (dto: ClientRegisterDto) =>
    api.post<AuthResponse>('/client/auth/register', dto).then(r => r.data),

  login: (dto: ClientLoginDto) =>
    api.post<AuthResponse>('/client/auth/login', dto).then(r => r.data),

  me: () =>
    api.get<ClientProfil>('/client/auth/me').then(r => r.data),

  updateProfil: (dto: { nom?: string; telephone?: string }) =>
    api.put<ClientProfil>('/client/auth/me', dto).then(r => r.data),
}
