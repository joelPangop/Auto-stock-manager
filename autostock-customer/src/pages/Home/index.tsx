import { Link } from 'react-router-dom'
import { Shield, Zap, Handshake, Headphones, ArrowRight, Phone, Mail, CheckCircle2 } from 'lucide-react'
import { VehicleCarousel } from '@/components/VehicleCarousel'

const categories = [
  { label: 'CITADINES', sub: 'Agiles et économiques' },
  { label: 'BERLINES',  sub: 'Confort et élégance' },
  { label: 'SUV / 4x4', sub: 'Puissance et espace' },
  { label: 'PREMIUM',   sub: 'Luxe et performance' },
]

const valeurs = [
  { icon: Shield,     label: 'QUALITÉ',        desc: 'Véhicules contrôlés et garantis' },
  { icon: Zap,        label: 'PERFORMANCE',    desc: 'Des voitures fiables et performantes' },
  { icon: Handshake,  label: 'CONFIANCE',      desc: "Une relation basée sur l'honnêteté" },
  { icon: Headphones, label: 'ACCOMPAGNEMENT', desc: 'Un service personnalisé de A à Z' },
]

const engagements = [
  'Inspection complète 150 points',
  'Historique Carfax disponible',
  'Financement flexible sur place',
  'Garantie incluse',
]

export default function Home() {
  return (
    <div className="bg-[#0a0a0a]">

      {/* ── HERO ─────────────────────────────────────────────── */}
      <section className="relative min-h-[620px] flex items-center overflow-hidden">

        {/* Fond noir uniforme */}
        <div className="absolute inset-0 bg-[#0a0a0a]">
          {/* Lignes horizontales décoratives */}
          <div className="absolute bottom-0 left-0 right-0 h-px bg-red-600/30" />
          <div className="absolute top-0 left-0 right-0 h-px bg-white/5" />
          {/* Grille de points subtile */}
          <div className="absolute inset-0 opacity-[0.03]"
            style={{
              backgroundImage: 'radial-gradient(circle, #fff 1px, transparent 1px)',
              backgroundSize: '32px 32px',
            }} />
        </div>

        <div className="relative max-w-7xl mx-auto px-6 py-24 w-full grid md:grid-cols-2 gap-12 items-center">

          {/* Texte gauche */}
          <div>
            <div className="inline-flex items-center gap-2 bg-red-600/20 border border-red-600/40 text-red-400 text-xs font-medium px-3 py-1.5 rounded mb-6 uppercase tracking-widest">
              <span className="w-1.5 h-1.5 bg-red-500 rounded-full animate-pulse" />
              Vente de voitures usagées
            </div>

            <h1 className="text-5xl md:text-6xl font-black text-white leading-tight mb-2">
              TROUVEZ LA VOITURE
            </h1>
            <h1 className="text-5xl md:text-6xl font-black text-red-600 leading-tight mb-6">
              QUI VOUS CORRESPOND
            </h1>

            <p className="text-gray-400 text-lg mb-8 leading-relaxed max-w-lg">
              Des véhicules sélectionnés avec soin pour vous offrir qualité, sécurité et tranquillité.
            </p>

            <div className="flex flex-wrap gap-4 mb-10">
              <Link to="/catalogue"
                className="bg-red-600 hover:bg-red-700 text-white font-bold px-8 py-3.5 rounded flex items-center gap-2 transition-all hover:translate-x-1">
                Découvrir nos véhicules
                <ArrowRight size={18} />
              </Link>
              <Link to="/contact"
                className="border border-gray-600 hover:border-white text-gray-300 hover:text-white font-semibold px-8 py-3.5 rounded transition-colors">
                Nous contacter
              </Link>
            </div>

            <div className="flex flex-wrap gap-6 pt-8 border-t border-white/10">
              <a href="tel:+14385072586"
                className="flex items-center gap-2 text-gray-400 hover:text-white text-sm transition-colors">
                <div className="w-8 h-8 bg-red-600/20 rounded-full flex items-center justify-center">
                  <Phone size={14} className="text-red-500" />
                </div>
                +1 (438) 507-2586
              </a>
              <a href="mailto:contact@tedauto.com"
                className="flex items-center gap-2 text-gray-400 hover:text-white text-sm transition-colors">
                <div className="w-8 h-8 bg-red-600/20 rounded-full flex items-center justify-center">
                  <Mail size={14} className="text-red-500" />
                </div>
                contact@tedauto.com
              </a>
            </div>
          </div>

          {/* Bloc décoratif droite — logo + accroche */}
          <div className="hidden md:flex flex-col items-center justify-center gap-6">
            <img
              src="/assets/logo-clean.png"
              alt="Ted Auto"
              className="w-80 object-contain"
              style={{ mixBlendMode: 'lighten', filter: 'contrast(1.1)' }}
              onError={e => { (e.target as HTMLImageElement).style.display = 'none' }}
            />
            <div className="flex flex-col items-center gap-1 text-center">
              <p className="text-white/30 text-xs tracking-[0.3em] uppercase">Votre confiance, notre priorité</p>
              <div className="w-16 h-px bg-red-600/50 mt-1" />
            </div>
          </div>

        </div>
      </section>

      {/* ── VALEURS ──────────────────────────────────────────── */}
      <section className="border-y border-[#1e1e1e] bg-[#0f0f0f]">
        <div className="max-w-7xl mx-auto px-6 py-14 grid grid-cols-2 md:grid-cols-4 gap-8">
          {valeurs.map(({ icon: Icon, label, desc }) => (
            <div key={label} className="flex flex-col items-center text-center gap-3">
              <div className="w-14 h-14 bg-red-600/10 border border-red-600/30 rounded-full flex items-center justify-center">
                <Icon size={24} className="text-red-500" />
              </div>
              <div>
                <p className="text-white font-bold text-sm tracking-wider uppercase">{label}</p>
                <p className="text-gray-500 text-xs mt-1 leading-relaxed">{desc}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ── CAROUSEL AUTO-DÉFILANT ───────────────────────────── */}
      <VehicleCarousel
        title="NOS VÉHICULES"
        subtitle="Découvrez notre inventaire"
      />

      {/* ── ENGAGEMENT + CATÉGORIES ──────────────────────────── */}
      <section className="max-w-7xl mx-auto px-6 py-16 grid md:grid-cols-2 gap-6">

        {/* Engagement */}
        <div className="rounded-2xl border border-[#1e1e1e] bg-[#111] p-8 flex flex-col justify-between min-h-[340px]">
          <div>
            <p className="text-red-500 text-xs font-bold tracking-widest uppercase mb-2">Pourquoi nous choisir</p>
            <h2 className="text-3xl font-black text-white mb-6">
              NOTRE <span className="text-red-600">ENGAGEMENT</span>
            </h2>
            <ul className="flex flex-col gap-3">
              {engagements.map(item => (
                <li key={item} className="flex items-center gap-3 text-gray-300 text-sm">
                  <CheckCircle2 size={16} className="text-red-500 shrink-0" />
                  {item}
                </li>
              ))}
            </ul>
          </div>
          <Link to="/catalogue"
            className="inline-flex items-center gap-2 bg-red-600 hover:bg-red-700 text-white font-bold text-sm px-5 py-2.5 rounded transition-colors mt-6 w-fit">
            Voir les véhicules <ArrowRight size={16} />
          </Link>
        </div>

        {/* Catégories */}
        <div className="rounded-2xl border border-[#1e1e1e] bg-[#111] p-8 flex flex-col justify-between min-h-[340px]">
          <div>
            <p className="text-red-500 text-xs font-bold tracking-widest uppercase mb-2">Notre sélection</p>
            <h2 className="text-3xl font-black text-white mb-6">
              NOS <span className="text-red-600">CATÉGORIES</span>
            </h2>
            <div className="grid grid-cols-2 gap-3">
              {categories.map(cat => (
                <Link key={cat.label}
                  to={`/catalogue?categorie=${cat.label.toLowerCase().replace('/', '').replace(' ', '-')}`}
                  className="bg-[#1a1a1a] hover:bg-red-600/20 border border-[#2a2a2a] hover:border-red-500/50 rounded-xl px-4 py-3 text-white text-xs font-bold tracking-wider transition-all group">
                  <div className="flex items-center gap-2 mb-0.5">
                    <div className="w-1.5 h-1.5 bg-red-600 rounded-full group-hover:scale-125 transition-transform" />
                    {cat.label}
                  </div>
                  <p className="text-gray-500 text-[10px] font-normal pl-3.5">{cat.sub}</p>
                </Link>
              ))}
            </div>
          </div>
          <Link to="/catalogue"
            className="inline-flex items-center gap-2 border border-[#2a2a2a] hover:border-white text-gray-300 hover:text-white font-bold text-sm px-5 py-2.5 rounded transition-colors mt-6 w-fit">
            Explorer le catalogue <ArrowRight size={16} />
          </Link>
        </div>
      </section>

      {/* ── STATS ─────────────────────────────────────────────── */}
      <section className="bg-[#0f0f0f] border-y border-[#1e1e1e] py-14">
        <div className="max-w-7xl mx-auto px-6 grid grid-cols-1 sm:grid-cols-3 gap-8 text-center">
          {[
            { value: '500+', label: 'Véhicules vendus' },
            { value: '98%',  label: 'Clients satisfaits' },
            { value: '10+',  label: "Années d'expérience" },
          ].map(s => (
            <div key={s.label}>
              <p className="text-4xl md:text-5xl font-black text-red-600 mb-1">{s.value}</p>
              <p className="text-gray-400 text-sm">{s.label}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── CTA FINAL ────────────────────────────────────────── */}
      <section className="bg-red-600 py-16">
        <div className="max-w-7xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-6">
          <div>
            <h2 className="text-3xl font-black text-white">Prêt à trouver votre future voiture ?</h2>
            <p className="text-red-100 mt-1">Contactez-nous dès aujourd'hui — notre équipe vous répond rapidement.</p>
          </div>
          <div className="flex flex-col sm:flex-row gap-4 shrink-0 w-full sm:w-auto">
            <a href="tel:+14385072586"
              className="bg-white text-red-600 font-bold px-6 py-3 rounded hover:bg-red-50 transition-colors text-sm flex items-center gap-2">
              <Phone size={16} />Appeler maintenant
            </a>
            <Link to="/catalogue"
              className="border-2 border-white text-white font-bold px-6 py-3 rounded hover:bg-white/10 transition-colors text-sm">
              Voir le catalogue
            </Link>
          </div>
        </div>
      </section>

    </div>
  )
}
