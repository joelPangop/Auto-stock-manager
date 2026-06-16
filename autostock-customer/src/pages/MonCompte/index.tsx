import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { reservationApi } from '@/api/reservation'
import { useAuthStore } from '@/store/authStore'
import { Link } from 'react-router-dom'
import { User, Calendar, X, Clock, CheckCircle } from 'lucide-react'
import type { Reservation } from '@/types'
import { useTranslation } from 'react-i18next'

function ReservationCard({ r }: { r: Reservation }) {
  const qc = useQueryClient()
  const { t } = useTranslation()
  const { mutate: annuler, isPending } = useMutation({
    mutationFn: () => reservationApi.annuler(r.id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['mes-reservations'] }),
  })

  const statutReservation: Record<string, { label: string; color: string; icon: React.ReactNode }> = {
    EN_ATTENTE: { label: t('compte.en_attente'), color: 'text-yellow-400', icon: <Clock size={14} /> },
    CONFIRMEE:  { label: t('compte.confirmee'),  color: 'text-green-400',  icon: <CheckCircle size={14} /> },
    ANNULEE:    { label: t('compte.annulee'),    color: 'text-red-400',    icon: <X size={14} /> },
  }

  const s = statutReservation[r.statut] ?? statutReservation['EN_ATTENTE']

  return (
    <div className="bg-[#1a1a1a] border border-[#2a2a2a] rounded-lg p-5 flex gap-4">
      <div className="w-24 h-16 bg-[#111] rounded-lg overflow-hidden shrink-0 flex items-center justify-center text-[#2a2a2a]">
        <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M8 17l-2-5H4l2-5h12l2 5h-2l-2 5H8z" />
        </svg>
      </div>

      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <p className="text-white font-semibold text-sm">{r.voitureLabel}</p>
          <span className={`flex items-center gap-1 text-xs font-medium ${s.color}`}>
            {s.icon}{s.label}
          </span>
        </div>

        <div className="flex items-center gap-4 mt-2 text-gray-500 text-xs flex-wrap">
          <span className="flex items-center gap-1">
            <Calendar size={12} />
            {t('compte.creee_le')} {new Date(r.createdAt).toLocaleDateString('fr-CA')}
          </span>
          {r.dateVisite && (
            <span className="flex items-center gap-1">
              <Clock size={12} />
              {t('compte.visite')} : {new Date(r.dateVisite).toLocaleDateString('fr-CA')}
            </span>
          )}
        </div>
        {r.message && (
          <p className="text-gray-500 text-xs mt-1.5 italic truncate">{r.message}</p>
        )}
      </div>

      <div className="flex flex-col gap-2 shrink-0 items-end">
        <Link to={`/catalogue/${r.voitureId}`}
          className="text-xs text-gray-400 hover:text-white transition-colors">{t('compte.voir')}</Link>
        {r.statut === 'EN_ATTENTE' && (
          <button onClick={() => annuler()} disabled={isPending}
            className="text-xs text-red-500 hover:text-red-400 disabled:opacity-50 transition-colors">
            {t('compte.annuler')}
          </button>
        )}
      </div>
    </div>
  )
}

export default function MonCompte() {
  const { client } = useAuthStore()
  const { t } = useTranslation()
  const { data: reservations, isLoading } = useQuery({
    queryKey: ['mes-reservations'],
    queryFn: reservationApi.mes,
  })

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10">

      <div className="mb-10">
        <p className="text-red-500 text-sm font-medium tracking-widest uppercase mb-1">{t('compte.espace_perso')}</p>
        <h1 className="text-3xl font-black text-white">{t('compte.mon')} <span className="text-red-600">{t('compte.compte')}</span></h1>
        <div className="w-12 h-0.5 bg-red-600 mt-3" />
      </div>

      <div className="bg-[#111] border border-[#2a2a2a] rounded-xl p-6 mb-6 flex items-center gap-4">
        <div className="w-14 h-14 bg-red-600/10 border border-red-600/30 rounded-full flex items-center justify-center">
          <User size={22} className="text-red-500" />
        </div>
        <div>
          <p className="text-white font-bold text-lg">{client?.nom}</p>
          <p className="text-gray-500 text-sm">{client?.email}</p>
          {client?.telephone && <p className="text-gray-500 text-sm">{client.telephone}</p>}
        </div>
      </div>

      <div>
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-white font-bold text-lg">{t('compte.mes_reservations')}</h2>
          <Link to="/catalogue"
            className="text-red-500 hover:text-red-400 text-sm transition-colors">
            + {t('compte.nouvelle_reservation')}
          </Link>
        </div>

        {isLoading ? (
          <div className="flex flex-col gap-3">
            {[1, 2].map(i => (
              <div key={i} className="bg-[#1a1a1a] border border-[#2a2a2a] rounded-lg p-5 animate-pulse flex gap-4">
                <div className="w-24 h-16 bg-[#111] rounded-lg" />
                <div className="flex-1 flex flex-col gap-2">
                  <div className="h-4 bg-[#111] rounded w-1/2" />
                  <div className="h-3 bg-[#111] rounded w-1/4" />
                </div>
              </div>
            ))}
          </div>
        ) : reservations?.length === 0 ? (
          <div className="bg-[#111] border border-[#2a2a2a] rounded-xl p-12 text-center">
            <p className="text-4xl mb-3">🚗</p>
            <p className="text-gray-400 font-medium">{t('compte.aucune_reservation')}</p>
            <Link to="/catalogue"
              className="mt-4 inline-block bg-red-600 hover:bg-red-700 text-white text-sm font-medium px-5 py-2.5 rounded-lg transition-colors">
              {t('compte.voir_catalogue')}
            </Link>
          </div>
        ) : (
          <div className="flex flex-col gap-3">
            {reservations?.map(r => <ReservationCard key={r.id} r={r} />)}
          </div>
        )}
      </div>
    </div>
  )
}
