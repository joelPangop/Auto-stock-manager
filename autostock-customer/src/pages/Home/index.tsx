import { Link } from 'react-router-dom'
import { Shield, Zap, Handshake, Headphones, ArrowRight, Phone, Mail, CheckCircle2 } from 'lucide-react'
import { VehicleCarousel } from '@/components/VehicleCarousel'
import { useTranslation } from 'react-i18next'

export default function Home() {
  const { t } = useTranslation()

  const categories = [
    { label: 'CITADINES', sub: t('home.categories.citadines') },
    { label: 'BERLINES',  sub: t('home.categories.berlines') },
    { label: 'SUV / 4x4', sub: t('home.categories.suv') },
    { label: 'PREMIUM',   sub: t('home.categories.premium') },
  ]

  const valeurs = [
    { icon: Shield,     label: t('home.valeurs.qualite'),        desc: t('home.valeurs.qualite_desc') },
    { icon: Zap,        label: t('home.valeurs.performance'),    desc: t('home.valeurs.performance_desc') },
    { icon: Handshake,  label: t('home.valeurs.confiance'),      desc: t('home.valeurs.confiance_desc') },
    { icon: Headphones, label: t('home.valeurs.accompagnement'), desc: t('home.valeurs.accompagnement_desc') },
  ]

  const engagements = [
    t('home.engagements.inspection'),
    t('home.engagements.carfax'),
    t('home.engagements.financement'),
    t('home.engagements.garantie'),
  ]

  return (
    <div className="bg-[#0a0a0a]">

      {/* ── HERO ───────────────────────────────────────────── */}
      <section className="relative min-h-[620px] flex items-center overflow-hidden">
        <div className="absolute inset-0 bg-[#0a0a0a]">
          <div className="absolute bottom-0 left-0 right-0 h-px bg-red-600/30" />
          <div className="absolute top-0 left-0 right-0 h-px bg-white/5" />
          <div className="absolute inset-0 opacity-[0.03]"
            style={{ backgroundImage: 'radial-gradient(circle, #fff 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
        </div>

        <div className="relative max-w-7xl mx-auto px-6 py-24 w-full grid md:grid-cols-2 gap-12 items-center">
          <div>
            <div className="inline-flex items-center gap-2 bg-red-600/20 border border-red-600/40 text-red-400 text-xs font-medium px-3 py-1.5 rounded mb-6 uppercase tracking-widest">
              <span className="w-1.5 h-1.5 bg-red-500 rounded-full animate-pulse" />
              {t('home.badge')}
            </div>

            <h1 className="text-5xl md:text-6xl font-black text-white leading-tight mb-2">
              {t('home.hero_titre1')}
            </h1>
            <h1 className="text-5xl md:text-6xl font-black text-red-600 leading-tight mb-6">
              {t('home.hero_titre2')}
            </h1>

            <p className="text-gray-400 text-lg mb-8 leading-relaxed max-w-lg">
              {t('home.hero_desc')}
            </p>

            <div className="flex flex-wrap gap-4 mb-10">
              <Link to="/catalogue"
                className="bg-red-600 hover:bg-red-700 text-white font-bold px-8 py-3.5 rounded flex items-center gap-2 transition-all hover:translate-x-1">
                {t('home.decouvrir')}
                <ArrowRight size={18} />
              </Link>
              <Link to="/contact"
                className="border border-gray-600 hover:border-white text-gray-300 hover:text-white font-semibold px-8 py-3.5 rounded transition-colors">
                {t('home.nous_contacter')}
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

          <div className="hidden md:flex flex-col items-center justify-center gap-6">
            <img src="/assets/logo-clean.png" alt="Ted Auto" className="w-80 object-contain"
              style={{ mixBlendMode: 'lighten', filter: 'contrast(1.1)' }}
              onError={e => { (e.target as HTMLImageElement).style.display = 'none' }} />
            <div className="flex flex-col items-center gap-1 text-center">
              <p className="text-white/30 text-xs tracking-[0.3em] uppercase">{t('home.confiance')}</p>
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

      {/* ── CAROUSEL ─────────────────────────────────────────── */}
      <VehicleCarousel title={t('home.nos_vehicules')} subtitle={t('home.inventaire')} />

      {/* ── ENGAGEMENT + CATÉGORIES ─────────────────────────── */}
      <section className="max-w-7xl mx-auto px-6 py-16 grid md:grid-cols-2 gap-6">

        <div className="rounded-2xl border border-[#1e1e1e] bg-[#111] p-8 flex flex-col justify-between min-h-[340px]">
          <div>
            <p className="text-red-500 text-xs font-bold tracking-widest uppercase mb-2">{t('home.pourquoi')}</p>
            <h2 className="text-3xl font-black text-white mb-6">
              {t('home.engagement_titre')} <span className="text-red-600">{t('home.engagement_accent')}</span>
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
            {t('home.voir_vehicules')} <ArrowRight size={16} />
          </Link>
        </div>

        <div className="rounded-2xl border border-[#1e1e1e] bg-[#111] p-8 flex flex-col justify-between min-h-[340px]">
          <div>
            <p className="text-red-500 text-xs font-bold tracking-widest uppercase mb-2">{t('home.selection')}</p>
            <h2 className="text-3xl font-black text-white mb-6">
              {t('home.categories_titre')} <span className="text-red-600">{t('home.categories_accent')}</span>
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
            {t('home.explorer')} <ArrowRight size={16} />
          </Link>
        </div>
      </section>

      {/* ── STATS ─────────────────────────────────────────────── */}
      <section className="bg-[#0f0f0f] border-y border-[#1e1e1e] py-14">
        <div className="max-w-7xl mx-auto px-6 grid grid-cols-1 sm:grid-cols-3 gap-8 text-center">
          {[
            { value: '500+', label: t('home.vehicules_vendus') },
            { value: '98%',  label: t('home.clients_satisfaits') },
            { value: '10+',  label: t('home.annees_experience') },
          ].map(s => (
            <div key={s.label}>
              <p className="text-4xl md:text-5xl font-black text-red-600 mb-1">{s.value}</p>
              <p className="text-gray-400 text-sm">{s.label}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── CTA FINAL ─────────────────────────────────────────── */}
      <section className="bg-red-600 py-16">
        <div className="max-w-7xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-6">
          <div>
            <h2 className="text-3xl font-black text-white">{t('home.pret')}</h2>
            <p className="text-red-100 mt-1">{t('home.cta_desc')}</p>
          </div>
          <div className="flex flex-col sm:flex-row gap-4 shrink-0 w-full sm:w-auto">
            <a href="tel:+14385072586"
              className="bg-white text-red-600 font-bold px-6 py-3 rounded hover:bg-red-50 transition-colors text-sm flex items-center gap-2">
              <Phone size={16} />{t('home.appeler')}
            </a>
            <Link to="/catalogue"
              className="border-2 border-white text-white font-bold px-6 py-3 rounded hover:bg-white/10 transition-colors text-sm">
              {t('home.voir_catalogue')}
            </Link>
          </div>
        </div>
      </section>

    </div>
  )
}
