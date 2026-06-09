import { useRef, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Gauge, Calendar } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { catalogueApi } from '@/api/catalogue'
import type { VehiculePublic } from '@/types'
import { ReservedSticker } from './ui/Badge'

/** Carte compacte pour le carousel */
function CarouselCard({ v }: { v: VehiculePublic }) {
  const photoUrl = v.photoId ? catalogueApi.getPhotoUrl(v.photoId) : null
  const isReserved = v.statut === 'RESERVEE'

  return (
    <Link
      to={`/catalogue/${v.id}`}
      className="group shrink-0 w-52 sm:w-64 bg-[#111] border border-[#2a2a2a] hover:border-red-600/60 rounded-xl overflow-hidden transition-all duration-300 hover:shadow-lg hover:shadow-red-600/10 hover:-translate-y-1"
    >
      {/* Photo */}
      <div className="relative h-40 bg-[#1a1a1a] overflow-hidden">
        {photoUrl ? (
          <img
            src={photoUrl}
            alt={`${v.marque} ${v.modele}`}
            className={`w-full h-full object-cover group-hover:scale-105 transition-transform duration-500 ${isReserved ? 'brightness-75' : ''}`}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <svg className="w-14 h-14 text-[#2a2a2a]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1}
                d="M8 17l-2-5H4l2-5h12l2 5h-2l-2 5H8z" />
            </svg>
          </div>
        )}
        {/* Sticker RÉSERVÉE */}
        {isReserved && <ReservedSticker />}

        {/* Prix badge */}
        {v.prixVente && (
          <div className="absolute bottom-0 right-0 bg-red-600 text-white text-xs font-black px-2.5 py-1">
            {v.prixVente.toLocaleString('fr-CA')} $
          </div>
        )}
      </div>

      {/* Infos */}
      <div className="p-4">
        <p className="text-red-500 text-xs font-semibold uppercase tracking-wider">{v.marque}</p>
        <p className="text-white font-bold text-sm mt-0.5 truncate">{v.modele}</p>
        <div className="flex items-center gap-3 mt-2 text-gray-500 text-xs">
          {v.annee && (
            <span className="flex items-center gap-1">
              <Calendar size={11} />{v.annee}
            </span>
          )}
          {v.kilometrage && (
            <span className="flex items-center gap-1">
              <Gauge size={11} />{v.kilometrage.toLocaleString('fr-CA')} km
            </span>
          )}
        </div>
      </div>
    </Link>
  )
}

interface Props {
  title?: string
  subtitle?: string
}

export function VehicleCarousel({ title = 'NOTRE INVENTAIRE', subtitle = 'Découvrez nos véhicules disponibles' }: Props) {
  const trackRef = useRef<HTMLDivElement>(null)
  const animRef = useRef<number>(0)
  const pausedRef = useRef(false)
  const [shuffled, setShuffled] = useState<VehiculePublic[]>([])

  const { data } = useQuery({
    queryKey: ['vehicules-carousel'],
    queryFn: () => catalogueApi.list({ size: 50, page: 0 }),
  })

  // Mélange aléatoire une seule fois à la réception des données
  useEffect(() => {
    if (data?.content && data.content.length > 0) {
      const arr = [...data.content]
      for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [arr[i], arr[j]] = [arr[j], arr[i]]
      }
      setShuffled(arr)
    }
  }, [data])

  // Auto-défilement fluide avec requestAnimationFrame
  useEffect(() => {
    const track = trackRef.current
    if (!track || shuffled.length === 0) return

    const SPEED = 0.6 // px par frame (~36px/sec à 60fps)

    const scroll = () => {
      if (!pausedRef.current && track) {
        track.scrollLeft += SPEED
        // Boucle infinie : quand on arrive à la moitié (clone), on reset au début
        const half = track.scrollWidth / 2
        if (track.scrollLeft >= half) {
          track.scrollLeft -= half
        }
      }
      animRef.current = requestAnimationFrame(scroll)
    }

    animRef.current = requestAnimationFrame(scroll)
    return () => cancelAnimationFrame(animRef.current)
  }, [shuffled])

  if (shuffled.length === 0) return null

  // Dupliquer les cartes pour un défilement infini sans saut
  const items = [...shuffled, ...shuffled]

  return (
    <section className="py-10 md:py-20 overflow-hidden w-full">
      {/* En-tête */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 mb-8 md:mb-10">
        <div className="flex items-end justify-between">
          <div>
            <p className="text-red-500 text-sm font-medium tracking-widest uppercase mb-2">{subtitle}</p>
            <h2 className="text-3xl md:text-4xl font-black text-white">
              {title.split(' ').map((word, i) =>
                i === title.split(' ').length - 1
                  ? <span key={i} className="text-red-600"> {word}</span>
                  : <span key={i}>{word} </span>
              )}
            </h2>
          </div>
          <Link to="/catalogue"
            className="text-red-500 hover:text-red-400 text-sm font-medium flex items-center gap-1 transition-colors shrink-0">
            Voir tout →
          </Link>
        </div>
        <div className="flex items-center gap-3 mt-4">
          <div className="h-px w-16 bg-red-600" />
          <div className="w-2 h-2 bg-red-600 rotate-45" />
          <div className="h-px w-16 bg-red-600" />
        </div>
      </div>

      {/* Piste de défilement — position relative pour les dégradés */}
      <div className="relative">
        <div
          ref={trackRef}
          className="flex gap-5 overflow-x-hidden px-6 cursor-grab select-none"
          style={{ scrollBehavior: 'auto' }}
          onMouseEnter={() => { pausedRef.current = true }}
          onMouseLeave={() => { pausedRef.current = false }}
        >
          {items.map((v, i) => (
            <CarouselCard key={`${v.id}-${i}`} v={v} />
          ))}
        </div>
        {/* Dégradés sur les bords pour l'effet fondu */}
        <div className="pointer-events-none absolute inset-y-0 left-0 w-16 md:w-24 bg-gradient-to-r from-[#0a0a0a] to-transparent" />
        <div className="pointer-events-none absolute inset-y-0 right-0 w-16 md:w-24 bg-gradient-to-l from-[#0a0a0a] to-transparent" />
      </div>
    </section>
  )
}
