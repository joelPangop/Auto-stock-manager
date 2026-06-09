import { useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Calendar, Gauge, Palette, Phone, Mail, ChevronLeft, ChevronRight, Expand } from 'lucide-react'
import { catalogueApi } from '@/api/catalogue'
import { useAuthStore } from '@/store/authStore'
import { statutBadge } from '@/components/ui/Badge'
import { ReservationModal } from './ReservationModal'
import { PhotoLightbox } from './PhotoLightbox'

export default function VehicleDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuthStore()
  const [photoIdx, setPhotoIdx] = useState(0)
  const [showReservation, setShowReservation] = useState(false)
  const [lightboxIdx, setLightboxIdx] = useState<number | null>(null)

  const { data: v, isLoading } = useQuery({
    queryKey: ['vehicule', id],
    queryFn: () => catalogueApi.getById(Number(id)),
    enabled: !!id,
  })

  if (isLoading) return (
    <div className="max-w-6xl mx-auto px-6 py-16 animate-pulse">
      <div className="h-8 bg-[#1a1a1a] rounded w-48 mb-8" />
      <div className="grid md:grid-cols-2 gap-10">
        <div className="aspect-video bg-[#1a1a1a] rounded-lg" />
        <div className="flex flex-col gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-4 bg-[#1a1a1a] rounded" style={{ width: `${60 + i * 5}%` }} />
          ))}
        </div>
      </div>
    </div>
  )

  if (!v) return (
    <div className="text-center py-24 text-gray-500">
      <p className="text-4xl mb-4">🚗</p>
      <p>Véhicule introuvable</p>
      <Link to="/catalogue" className="text-red-500 text-sm mt-2 inline-block">← Retour au catalogue</Link>
    </div>
  )

  const photoIds = v.photoIds ?? []
  const currentPhotoUrl = photoIds.length > 0 ? catalogueApi.getPhotoUrl(photoIds[photoIdx]) : null
  const disponible = v.statut === 'EN_STOCK'

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-10">

      {/* Breadcrumb */}
      <button onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-gray-500 hover:text-white text-sm mb-8 transition-colors">
        <ArrowLeft size={16} />Retour au catalogue
      </button>

      <div className="grid md:grid-cols-2 gap-6 md:gap-10 w-full">

        {/* ── Photos ── sticky pendant le scroll */}
        <div className="flex flex-col gap-3 self-start md:sticky md:top-4 min-w-0 w-full overflow-hidden">
          {/* Photo principale */}
          <div className="relative aspect-video bg-[#1a1a1a] rounded-lg overflow-hidden group cursor-pointer"
            onClick={() => photoIds.length > 0 && setLightboxIdx(photoIdx)}>
            {currentPhotoUrl ? (
              <>
                <img src={currentPhotoUrl} alt={`${v.marque} ${v.modele}`}
                  className="w-full h-full object-cover" />
                {/* Overlay "Agrandir" au survol */}
                <div className="absolute inset-0 bg-black/0 group-hover:bg-black/30 transition-colors flex items-center justify-center">
                  <div className="opacity-0 group-hover:opacity-100 transition-opacity bg-black/70 text-white text-xs px-3 py-1.5 rounded-full flex items-center gap-1.5">
                    <Expand size={12} /> Agrandir
                  </div>
                </div>
              </>
            ) : (
              <div className="w-full h-full flex items-center justify-center text-[#2a2a2a]">
                <svg className="w-20 h-20" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1}
                    d="M8 17l-2-5H4l2-5h12l2 5h-2l-2 5H8z" />
                </svg>
              </div>
            )}
            {photoIds.length > 1 && (
              <>
                <button onClick={e => { e.stopPropagation(); setPhotoIdx(i => (i - 1 + photoIds.length) % photoIds.length) }}
                  className="absolute left-2 top-1/2 -translate-y-1/2 bg-black/60 hover:bg-black/80 text-white p-1.5 rounded-full transition-colors">
                  <ChevronLeft size={18} />
                </button>
                <button onClick={e => { e.stopPropagation(); setPhotoIdx(i => (i + 1) % photoIds.length) }}
                  className="absolute right-2 top-1/2 -translate-y-1/2 bg-black/60 hover:bg-black/80 text-white p-1.5 rounded-full transition-colors">
                  <ChevronRight size={18} />
                </button>
                <div className="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-1">
                  {photoIds.map((_, i) => (
                    <button key={i} onClick={e => { e.stopPropagation(); setPhotoIdx(i) }}
                      className={`w-1.5 h-1.5 rounded-full transition-colors ${i === photoIdx ? 'bg-red-500' : 'bg-white/40'}`} />
                  ))}
                </div>
              </>
            )}
          </div>

          {/* Miniatures */}
          {photoIds.length > 1 && (
            <div className="flex gap-2 overflow-x-auto pb-1">
              {photoIds.map((pid, i) => (
                <button key={pid} onClick={() => setPhotoIdx(i)}
                  className={`shrink-0 w-16 h-12 rounded overflow-hidden border-2 transition-colors ${
                    i === photoIdx ? 'border-red-600' : 'border-transparent'}`}>
                  <img src={catalogueApi.getPhotoUrl(pid)} alt="" className="w-full h-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* ── Détails ── */}
        <div className="flex flex-col gap-6 min-w-0 w-full overflow-hidden">
          <div>
            <div className="flex items-start justify-between gap-3 mb-1">
              <div>
                <p className="text-red-500 text-sm font-medium tracking-wider uppercase">{v.marque}</p>
                <h1 className="text-3xl font-black text-white">{v.modele}</h1>
              </div>
              {statutBadge(v.statut)}
            </div>
            <div className="w-12 h-0.5 bg-red-600 mt-3" />
          </div>

          {/* Prix */}
          {v.prixVente && (
            <div className="bg-red-600 rounded-lg px-6 py-4">
              <p className="text-red-100 text-xs uppercase tracking-wider mb-0.5">Prix de vente</p>
              <p className="text-white font-black text-3xl">{v.prixVente.toLocaleString('fr-CA')} $</p>
            </div>
          )}

          {/* Caractéristiques */}
          <div className="grid grid-cols-2 gap-3">
            {[
              { icon: Calendar, label: 'Année', value: v.annee },
              { icon: Gauge,    label: 'Kilométrage', value: v.kilometrage ? `${v.kilometrage.toLocaleString('fr-CA')} km` : '—' },
              { icon: Palette,  label: 'Couleur', value: v.couleur ?? '—' },
            ].map(({ icon: Icon, label, value }) => (
              <div key={label} className="bg-[#1a1a1a] border border-[#2a2a2a] rounded-lg p-4">
                <div className="flex items-center gap-2 mb-1">
                  <Icon size={14} className="text-red-500" />
                  <p className="text-gray-500 text-xs uppercase tracking-wider">{label}</p>
                </div>
                <p className="text-white font-semibold">{value}</p>
              </div>
            ))}
          </div>

          {/* Description */}
          {v.description && (
            <div className="bg-[#111] border border-[#2a2a2a] rounded-lg p-5">
              <h3 className="text-white font-bold text-sm uppercase tracking-wider mb-3 flex items-center gap-2">
                <span className="w-1 h-4 bg-red-600 rounded-full inline-block" />
                Description
              </h3>
              <p className="text-gray-400 text-sm leading-relaxed whitespace-pre-wrap break-words overflow-wrap-anywhere">{v.description}</p>
            </div>
          )}

          {/* Actions */}
          <div className="flex flex-col gap-3">
            {disponible && (
              <button
                onClick={() => isAuthenticated ? setShowReservation(true) : navigate('/login')}
                className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-3 rounded-lg transition-colors">
                {isAuthenticated ? 'Réserver ce véhicule' : 'Connectez-vous pour réserver'}
              </button>
            )}
            <div className="grid grid-cols-2 gap-3">
              <a href="tel:+14385072586"
                className="flex items-center justify-center gap-2 border border-[#2a2a2a] hover:border-red-600 text-gray-300 hover:text-white py-2.5 rounded-lg text-sm transition-colors">
                <Phone size={16} className="text-red-500" />
                Appeler
              </a>
              <Link to={`/contact?vehiculeId=${v.id}`}
                className="flex items-center justify-center gap-2 border border-[#2a2a2a] hover:border-red-600 text-gray-300 hover:text-white py-2.5 rounded-lg text-sm transition-colors">
                <Mail size={16} className="text-red-500" />
                Envoyer un message
              </Link>
            </div>
          </div>
        </div>
      </div>

      {showReservation && v && (
        <ReservationModal vehiculeId={v.id} vehiculeLabel={`${v.marque} ${v.modele} (${v.annee})`} onClose={() => setShowReservation(false)} />
      )}

      {lightboxIdx !== null && v && (
        <PhotoLightbox
          photoIds={photoIds}
          initialIndex={lightboxIdx}
          vehiculeLabel={`${v.marque} ${v.modele} (${v.annee})`}
          onClose={() => setLightboxIdx(null)}
        />
      )}
    </div>
  )
}
