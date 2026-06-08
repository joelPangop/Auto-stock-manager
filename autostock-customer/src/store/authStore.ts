import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { ClientProfil } from '@/types'

interface AuthState {
  token: string | null
  client: ClientProfil | null
  isAuthenticated: boolean
  login: (token: string, client: ClientProfil) => void
  logout: () => void
  setClient: (client: ClientProfil) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      client: null,
      isAuthenticated: false,

      login: (token, client) => {
        localStorage.setItem('customer_token', token)
        set({ token, client, isAuthenticated: true })
      },

      logout: () => {
        localStorage.removeItem('customer_token')
        set({ token: null, client: null, isAuthenticated: false })
      },

      setClient: (client) => set({ client }),
    }),
    { name: 'autostock-customer-auth', partialize: (s) => ({ token: s.token, client: s.client, isAuthenticated: s.isAuthenticated }) }
  )
)
