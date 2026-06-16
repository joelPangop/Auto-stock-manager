import { useState } from 'react'
import { X, CheckCircle } from 'lucide-react'
import { useMutation } from '@tanstack/react-query'
import { reservationApi } from '@/api/reservation'
import { useTranslation } from 'react-i18next'

interface Props {
  vehiculeId: number
  vehiculeLabel: string
  onClose: () => void
}

export function ReservationModal({ vehiculeId, vehiculeLabel, onClose }: Props) {
  const [message, setMessage] = useState('')
  const [dateVisite, setDateVisite] = useState('')
  const [done, setDone] = useState(false)
  const { t } = useTranslation()

  const { mutate, isPending, error } = useMutation({
    mutationFn: () => reservationApi.create({
      voitureId: vehiculeId,
      message: message || undefined,
      dateVisite: dateVisite || undefined,
    }),
    onSuccess: () => setDone(true),
  })

  return (
    <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      onClick={onClose}>
      <div className="bg-[#111] border border-[#2a2a2a] rounded-xl w-full max-w-md p-6"
        onClick={e => e.stopPropagation()}>

        {done ? (
          <div className="text-center py-6">
            <CheckCircle size={48} className="text-green-500 mx-auto mb-4" />
            <h3 className="text-white font-bold text-xl mb-2">{t('reservation.envoyee')}</h3>
            <p className="text-gray-400 text-sm mb-6">
              {t('reservation.envoyee_desc')}
            </p>
            <button onClick={onClose}
              className="bg-red-600 hover:bg-red-700 text-white font-medium px-6 py-2.5 rounded-lg transition-colors">
              {t('reservation.fermer')}
            </button>
          </div>
        ) : (
          <>
            <div className="flex items-center justify-between mb-5">
              <div>
                <h3 className="text-white font-bold text-lg">{t('reservation.titre')}</h3>
                <p className="text-red-500 text-sm">{vehiculeLabel}</p>
              </div>
              <button onClick={onClose} className="text-gray-500 hover:text-white">
                <X size={20} />
              </button>
            </div>

            {error && (
              <div className="mb-4 bg-red-950/50 border border-red-800 text-red-400 text-sm rounded-lg px-3 py-2">
                {t('reservation.erreur')}
              </div>
            )}

            <div className="flex flex-col gap-4">
              <div>
                <label className="text-gray-400 text-xs uppercase tracking-wider mb-1.5 block">
                  {t('reservation.date_visite')}
                </label>
                <input type="date" value={dateVisite}
                  onChange={e => setDateVisite(e.target.value)}
                  min={new Date().toISOString().split('T')[0]}
                  className="w-full bg-[#1a1a1a] border border-[#2a2a2a] focus:border-red-600 text-white rounded-lg px-3 py-2.5 text-sm focus:outline-none transition-colors" />
              </div>

              <div>
                <label className="text-gray-400 text-xs uppercase tracking-wider mb-1.5 block">
                  {t('reservation.message')}
                </label>
                <textarea value={message} onChange={e => setMessage(e.target.value)}
                  rows={3} placeholder={t('reservation.message_placeholder')}
                  className="w-full bg-[#1a1a1a] border border-[#2a2a2a] focus:border-red-600 text-white placeholder-gray-600 rounded-lg px-3 py-2.5 text-sm focus:outline-none resize-none transition-colors" />
              </div>

              <button onClick={() => mutate()} disabled={isPending}
                className="w-full bg-red-600 hover:bg-red-700 disabled:opacity-50 text-white font-bold py-3 rounded-lg transition-colors">
                {isPending ? t('reservation.envoi') : t('reservation.confirmer')}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
