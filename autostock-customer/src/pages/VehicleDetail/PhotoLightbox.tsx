import { useEffect, useRef, useState } from 'react'
import { X, ChevronLeft, ChevronRight, Send, Phone, Mail, User, MessageSquare, CheckCircle2, Camera, MessageCircle } from 'lucide-react'
import { useMutation } from '@tanstack/react-query'
import { catalogueApi } from '@/api/catalogue'
import { contactApi } from '@/api/contact'

interface Props {
  photoIds: number[]
  initialIndex: number
  vehiculeLabel: string
  onClose: () => void
}

export function PhotoLightbox({ photoIds, initialIndex, vehiculeLabel, onClose }: Props) {
  const [idx, setIdx] = useState(initialIndex)
  const [mobileTab, setMobileTab] = useState<'photos' | 'contact'>('photos')
  const [nom, setNom] = useState('')
  const [email, setEmail] = useState('')
  const [telephone, setTelephone] = useState('')
  const [message, setMessage] = useState(`Bonjour, je suis intéressé(e) par le véhicule ${vehiculeLabel}. Est-il toujours disponible ?`)
  const [sent, setSent] = useState(false)
  const thumbsRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
      if (e.key === 'ArrowLeft')  setIdx(i => (i - 1 + photoIds.length) % photoIds.length)
      if (e.key === 'ArrowRight') setIdx(i => (i + 1) % photoIds.length)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [photoIds.length, onClose])

  useEffect(() => {
    const container = thumbsRef.current
    if (!container) return
    const thumb = container.children[idx] as HTMLElement
    if (thumb) thumb.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' })
  }, [idx])

  const prev = () => setIdx(i => (i - 1 + photoIds.length) % photoIds.length)
  const next = () => setIdx(i => (i + 1) % photoIds.length)

  const { mutate: sendContact, isPending: isLoading } = useMutation({
    mutationFn: () => contactApi.envoyer({
      nom, email, telephone,
      sujet: `Demande d'info – ${vehiculeLabel}`,
      message,
    }),
    onSuccess: () => setSent(true),
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!nom.trim() || !email.trim() || !message.trim()) return
    sendContact()
  }

  const ContactForm = () => (
    <div className="p-5 flex flex-col gap-4">
      <div>
        <h3 className="text-white font-black text-lg">Contacter le vendeur</h3>
        <p className="text-gray-500 text-xs mt-1">{vehiculeLabel}</p>
      </div>

      {sent ? (
        <div className="flex flex-col items-center gap-3 py-8 text-center">
          <CheckCircle2 size={48} className="text-green-500" />
          <p className="text-white font-bold">Message envoyé !</p>
          <p className="text-gray-400 text-sm">Nous vous répondrons dans les plus brefs délais.</p>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <label className="flex items-center gap-1.5 text-gray-400 text-xs uppercase tracking-wider">
              <MessageSquare size={12} className="text-red-500" /> Message *
            </label>
            <textarea rows={4} value={message} onChange={e => setMessage(e.target.value)} required
              className="bg-[#1a1a1a] border border-[#2a2a2a] focus:border-red-600 text-white text-sm rounded-lg px-3 py-2.5 resize-none outline-none transition-colors" />
          </div>
          <div className="flex flex-col gap-1">
            <label className="flex items-center gap-1.5 text-gray-400 text-xs uppercase tracking-wider">
              <User size={12} className="text-red-500" /> Nom *
            </label>
            <input type="text" value={nom} onChange={e => setNom(e.target.value)} required
              className="bg-[#1a1a1a] border border-[#2a2a2a] focus:border-red-600 text-white text-sm rounded-lg px-3 py-2.5 outline-none transition-colors"
              placeholder="Votre nom" />
          </div>
          <div className="flex flex-col gap-1">
            <label className="flex items-center gap-1.5 text-gray-400 text-xs uppercase tracking-wider">
              <Mail size={12} className="text-red-500" /> Email *
            </label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)} required
              className="bg-[#1a1a1a] border border-[#2a2a2a] focus:border-red-600 text-white text-sm rounded-lg px-3 py-2.5 outline-none transition-colors"
              placeholder="votre@email.com" />
          </div>
          <div className="flex flex-col gap-1">
            <label className="flex items-center gap-1.5 text-gray-400 text-xs uppercase tracking-wider">
              <Phone size={12} className="text-red-500" /> Téléphone
            </label>
            <input type="tel" value={telephone} onChange={e => setTelephone(e.target.value)}
              className="bg-[#1a1a1a] border border-[#2a2a2a] focus:border-red-600 text-white text-sm rounded-lg px-3 py-2.5 outline-none transition-colors"
              placeholder="+1 (514) 000-0000" />
          </div>
          <button type="submit" disabled={isLoading}
            className="w-full bg-red-600 hover:bg-red-700 disabled:opacity-50 text-white font-bold py-3 rounded-lg flex items-center justify-center gap-2 transition-colors text-sm">
            <Send size={15} />
            {isLoading ? 'Envoi…' : 'Envoyer le message'}
          </button>
        </form>
      )}

      <div className="border-t border-white/10 pt-4">
        <a href="tel:+14385072586"
          className="flex items-center justify-center gap-2 border border-[#2a2a2a] hover:border-red-600 text-gray-300 hover:text-white py-2.5 rounded-lg text-sm transition-colors">
          <Phone size={14} className="text-red-500" /> +1 (438) 507-2586
        </a>
      </div>
    </div>
  )

  return (
    <div className="fixed inset-0 z-50 bg-black/95 flex flex-col">

      {/* ── Barre du haut ── */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-white/10 shrink-0">
        <button onClick={onClose}
          className="flex items-center gap-2 text-gray-400 hover:text-white text-sm transition-colors">
          <X size={16} /> Fermer
        </button>
        <span className="text-gray-500 text-sm">{idx + 1} / {photoIds.length}</span>
        {/* Onglets mobile */}
        <div className="flex md:hidden gap-1">
          <button onClick={() => setMobileTab('photos')}
            className={`p-2 rounded text-xs flex items-center gap-1 transition-colors ${mobileTab === 'photos' ? 'text-white bg-white/10' : 'text-gray-500'}`}>
            <Camera size={14} />
          </button>
          <button onClick={() => setMobileTab('contact')}
            className={`p-2 rounded text-xs flex items-center gap-1 transition-colors ${mobileTab === 'contact' ? 'text-white bg-white/10' : 'text-gray-500'}`}>
            <MessageCircle size={14} />
          </button>
        </div>
        <div className="hidden md:block w-16" />
      </div>

      {/* ── Corps ── */}
      <div className="flex flex-1 overflow-hidden">

        {/* ── Galerie (toujours visible sur desktop, onglet sur mobile) ── */}
        <div className={`flex-1 flex-col overflow-hidden ${mobileTab === 'photos' ? 'flex' : 'hidden md:flex'}`}>

          {/* Photo principale */}
          <div className="flex-1 relative flex items-center justify-center overflow-hidden px-10 md:px-12">
            <img
              src={catalogueApi.getPhotoUrl(photoIds[idx])}
              alt={`${vehiculeLabel} – photo ${idx + 1}`}
              className="max-h-full max-w-full object-contain select-none"
            />
            {photoIds.length > 1 && (
              <>
                <button onClick={prev}
                  className="absolute left-1 md:left-2 top-1/2 -translate-y-1/2 bg-black/60 hover:bg-black/90 text-white p-2 md:p-3 rounded-full transition-colors">
                  <ChevronLeft size={20} />
                </button>
                <button onClick={next}
                  className="absolute right-1 md:right-2 top-1/2 -translate-y-1/2 bg-black/60 hover:bg-black/90 text-white p-2 md:p-3 rounded-full transition-colors">
                  <ChevronRight size={20} />
                </button>
              </>
            )}
          </div>

          {/* Barre de miniatures */}
          {photoIds.length > 1 && (
            <div ref={thumbsRef}
              className="flex gap-2 overflow-x-auto px-4 py-3 border-t border-white/10 shrink-0">
              {photoIds.map((pid, i) => (
                <button key={pid} onClick={() => setIdx(i)}
                  className={`shrink-0 w-14 h-10 md:w-16 md:h-12 rounded overflow-hidden border-2 transition-all ${
                    i === idx ? 'border-red-600 opacity-100' : 'border-transparent opacity-50 hover:opacity-80'
                  }`}>
                  <img src={catalogueApi.getPhotoUrl(pid)} alt="" className="w-full h-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* ── Formulaire contact ── */}
        {/* Desktop : panneau fixe à droite | Mobile : onglet scrollable */}
        <div className={`
          md:w-[340px] md:shrink-0 md:border-l md:border-white/10 md:flex md:flex-col md:overflow-y-auto md:bg-[#0d0d0d]
          ${mobileTab === 'contact' ? 'flex flex-col flex-1 overflow-y-auto bg-[#0d0d0d]' : 'hidden md:flex md:flex-col'}
        `}>
          <ContactForm />
        </div>
      </div>

      {/* ── Barre onglets mobile (bas) ── */}
      <div className="md:hidden flex border-t border-white/10 shrink-0">
        <button onClick={() => setMobileTab('photos')}
          className={`flex-1 py-3 text-sm font-medium flex items-center justify-center gap-2 transition-colors ${
            mobileTab === 'photos' ? 'text-white border-t-2 border-red-600 -mt-px' : 'text-gray-500'
          }`}>
          <Camera size={16} /> Photos
        </button>
        <button onClick={() => setMobileTab('contact')}
          className={`flex-1 py-3 text-sm font-medium flex items-center justify-center gap-2 transition-colors ${
            mobileTab === 'contact' ? 'text-white border-t-2 border-red-600 -mt-px' : 'text-gray-500'
          }`}>
          <MessageCircle size={16} /> Contacter
        </button>
      </div>

    </div>
  )
}
