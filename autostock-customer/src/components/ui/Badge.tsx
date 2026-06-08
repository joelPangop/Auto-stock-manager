import { clsx } from 'clsx'

type BadgeVariant = 'green' | 'yellow' | 'red' | 'blue' | 'gray'

interface BadgeProps {
  variant?: BadgeVariant
  children: React.ReactNode
  className?: string
}

export function Badge({ variant = 'gray', children, className }: BadgeProps) {
  return (
    <span className={clsx(
      'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
      {
        'bg-green-100 text-green-800': variant === 'green',
        'bg-yellow-100 text-yellow-800': variant === 'yellow',
        'bg-red-100 text-red-800': variant === 'red',
        'bg-blue-100 text-blue-800': variant === 'blue',
        'bg-gray-100 text-gray-700': variant === 'gray',
      },
      className
    )}>
      {children}
    </span>
  )
}

/** Badge simple affiché dans les cards / détail (coin supérieur gauche) */
export function statutBadge(statut: string) {
  if (statut === 'EN_STOCK') {
    return (
      <span className="inline-flex items-center gap-1 bg-emerald-900/80 border border-emerald-500/50 text-emerald-400 text-xs font-bold px-2.5 py-1 rounded-full backdrop-blur-sm">
        <span className="w-1.5 h-1.5 bg-emerald-400 rounded-full animate-pulse" />
        Disponible
      </span>
    )
  }
  if (statut === 'RESERVEE') {
    return (
      <span className="inline-flex items-center gap-1 bg-amber-900/80 border border-amber-500/50 text-amber-300 text-xs font-bold px-2.5 py-1 rounded-full backdrop-blur-sm">
        <span className="w-1.5 h-1.5 bg-amber-400 rounded-full" />
        Réservée
      </span>
    )
  }
  return (
    <span className="inline-flex items-center gap-1 bg-zinc-800/80 border border-zinc-600/50 text-zinc-400 text-xs font-medium px-2.5 py-1 rounded-full">
      {statut}
    </span>
  )
}

/**
 * Sticker grand format positionné en diagonale sur l'image de la card.
 * À utiliser uniquement pour les véhicules RÉSERVÉE.
 */
export function ReservedSticker() {
  return (
    <div className="absolute inset-0 pointer-events-none overflow-hidden rounded-t-lg">
      {/* Bandeau diagonal */}
      <div
        className="absolute -right-10 top-5 w-44 bg-amber-500 text-black text-xs font-black tracking-widest uppercase text-center py-1.5 shadow-lg"
        style={{ transform: 'rotate(35deg)', transformOrigin: 'center' }}
      >
        RÉSERVÉE
      </div>
      {/* Légère teinte orange sur la photo */}
      <div className="absolute inset-0 bg-amber-900/20" />
    </div>
  )
}
