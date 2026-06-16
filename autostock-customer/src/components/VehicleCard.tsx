import { Link } from 'react-router-dom'
import { Gauge, Calendar, Tag } from 'lucide-react'
import type { VehiculePublic } from '@/types'
import { statutBadge, ReservedSticker } from './ui/Badge'
import { catalogueApi } from '@/api/catalogue'
import { useTranslation } from 'react-i18next'

interface Props { vehicule: VehiculePublic }

export function VehicleCard({ vehicule: v }: Props) {
  const photoUrl = v.photoId ? catalogueApi.getPhotoUrl(v.photoId) : null
  const isReserved = v.statut === 'RESERVEE'
  const { t } = useTranslation()

  return (
    <Link to={`/catalogue/${v.id}`}
      className="group bg-[#111] border border-[#2a2a2a] hover:border-red-600/50 rounded-lg overflow-hidden transition-all duration-200 hover:shadow-lg hover:shadow-red-600/10 flex flex-col">

      {/* Photo */}
      <div className="relative aspect-[16/10] overflow-hidden bg-[#1a1a1a]">
        {photoUrl ? (
          <img src={photoUrl} alt={`${v.marque} ${v.modele}`}
            className={`w-full h-full object-cover group-hover:scale-105 transition-transform duration-500 ${isReserved ? 'brightness-75' : ''}`} />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <svg className="w-16 h-16 text-[#2a2a2a]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1}
                d="M8 17l-2-5H4l2-5h12l2 5h-2l-2 5H8z" />
            </svg>
          </div>
        )}

        {/* Sticker RÉSERVÉE en diagonale */}
        {isReserved && <ReservedSticker />}

        {/* Badge statut (coin haut gauche) — masqué si RÉSERVÉE car le sticker suffit */}
        {!isReserved && (
          <div className="absolute top-2 left-2">
            {statutBadge(v.statut)}
          </div>
        )}

        {/* Prix overlay */}
        {v.prixVente && (
          <div className="absolute bottom-0 right-0 bg-red-600 text-white font-black text-sm px-3 py-1">
            {v.prixVente.toLocaleString('fr-CA')} $
          </div>
        )}
      </div>

      {/* Infos */}
      <div className="p-4 flex flex-col gap-3 flex-1">
        <div>
          <p className="text-xs text-red-500 font-medium uppercase tracking-wider">{v.marque}</p>
          <h3 className="text-white font-bold text-base leading-tight">{v.modele}</h3>
        </div>

        <div className="flex items-center gap-4 text-gray-500 text-xs">
          {v.annee && (
            <span className="flex items-center gap-1">
              <Calendar size={12} />{v.annee}
            </span>
          )}
          {v.kilometrage && (
            <span className="flex items-center gap-1">
              <Gauge size={12} />{v.kilometrage.toLocaleString('fr-CA')} km
            </span>
          )}
          {v.couleur && (
            <span className="flex items-center gap-1">
              <Tag size={12} />{v.couleur}
            </span>
          )}
        </div>

        <div className="mt-auto pt-3 border-t border-[#2a2a2a] flex items-center justify-between">
          <span className="text-red-500 text-xs font-medium flex items-center gap-1 group-hover:gap-2 transition-all">
            {t('vehicule.voir_details')} →
          </span>
          {isReserved && (
            <span className="text-amber-500/70 text-xs font-medium">{t('vehicule.reservee')}</span>
          )}
        </div>
      </div>
    </Link>
  )
}
