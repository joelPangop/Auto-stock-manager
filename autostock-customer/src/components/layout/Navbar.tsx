import { Link, useNavigate, useLocation } from 'react-router-dom'
import { Menu, X, LogOut, User, Phone, Globe } from 'lucide-react'
import { useState } from 'react'
import { useAuthStore } from '@/store/authStore'
import { clsx } from 'clsx'
import { useTranslation } from 'react-i18next'

export function Navbar() {
  const [open, setOpen] = useState(false)
  const [langOpen, setLangOpen] = useState(false)
  const { isAuthenticated, client, logout } = useAuthStore()
  const navigate = useNavigate()
  const location = useLocation()
  const { t, i18n } = useTranslation()

  const handleLogout = () => { logout(); navigate('/') }

  const setLang = (lang: string) => {
    i18n.changeLanguage(lang)
    localStorage.setItem('i18nextLng', lang)
    setLangOpen(false)
  }

  const links = [
    { to: '/',          label: t('nav.accueil') },
    { to: '/catalogue', label: t('nav.catalogue') },
    { to: '/contact',   label: t('nav.contact') },
  ]

  const isActive = (to: string) =>
    to === '/' ? location.pathname === '/' : location.pathname.startsWith(to)

  return (
    <>
      {/* Barre supérieure rouge */}
      <div className="bg-red-600 text-white text-xs py-1.5 hidden md:block">
        <div className="max-w-7xl mx-auto px-6 flex justify-between items-center">
          <span className="flex items-center gap-1.5">
            <Phone size={12} />+1 (438) 507-2586
          </span>
          <span>contact@tedauto.com</span>
        </div>
      </div>

      {/* Navbar principale */}
      <nav className="bg-[#111] border-b border-[#2a2a2a] sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">

            {/* Logo */}
            <Link to="/" className="flex items-center gap-3">
              <img src="/assets/logo.jpg" alt="Ted Auto"
                className="h-10 w-auto object-contain"
                onError={e => { (e.target as HTMLImageElement).style.display = 'none' }} />
              <div className="hidden sm:block">
                <div className="text-white font-bold text-lg leading-none tracking-wide">TED AUTO</div>
                <div className="text-red-600 text-[10px] tracking-widest uppercase">{t('nav.slogan')}</div>
              </div>
            </Link>

            {/* Links desktop */}
            <div className="hidden md:flex items-center gap-1">
              {links.map(l => (
                <Link key={l.to} to={l.to}
                  className={clsx(
                    'px-4 py-2 text-sm font-medium rounded transition-colors',
                    isActive(l.to)
                      ? 'text-red-500 bg-white/5'
                      : 'text-gray-300 hover:text-white hover:bg-white/5'
                  )}>
                  {l.label}
                </Link>
              ))}
            </div>

            {/* Auth + Lang desktop */}
            <div className="hidden md:flex items-center gap-3">
              {isAuthenticated ? (
                <>
                  <Link to="/mon-compte"
                    className="flex items-center gap-2 text-sm text-gray-300 hover:text-white transition-colors">
                    <User size={16} />
                    <span>{client?.nom}</span>
                  </Link>
                  <button onClick={handleLogout}
                    className="p-2 text-gray-500 hover:text-red-500 transition-colors">
                    <LogOut size={16} />
                  </button>
                </>
              ) : (
                <>
                  <Link to="/login"
                    className="text-sm text-gray-300 hover:text-white transition-colors px-3 py-1.5">
                    {t('nav.connexion')}
                  </Link>
                  <Link to="/register"
                    className="bg-red-600 hover:bg-red-700 text-white text-sm font-medium px-4 py-2 rounded transition-colors">
                    {t('nav.inscrire')}
                  </Link>
                </>
              )}

              {/* Sélecteur de langue */}
              <div className="relative">
                <button
                  onClick={() => setLangOpen(!langOpen)}
                  className="flex items-center gap-1.5 text-gray-400 hover:text-white text-xs px-2 py-1.5 rounded border border-[#2a2a2a] hover:border-gray-500 transition-colors">
                  <Globe size={13} />
                  {i18n.language === 'en' ? 'EN' : 'FR'}
                </button>
                {langOpen && (
                  <div className="absolute right-0 mt-1 bg-[#1a1a1a] border border-[#2a2a2a] rounded shadow-lg z-50 min-w-[110px]">
                    <button onClick={() => setLang('fr')}
                      className={clsx('w-full text-left px-3 py-2 text-xs hover:bg-white/5 transition-colors', i18n.language === 'fr' ? 'text-red-500 font-bold' : 'text-gray-300')}>
                      🇫🇷 {t('lang.fr')}
                    </button>
                    <button onClick={() => setLang('en')}
                      className={clsx('w-full text-left px-3 py-2 text-xs hover:bg-white/5 transition-colors', i18n.language === 'en' ? 'text-red-500 font-bold' : 'text-gray-300')}>
                      🇬🇧 {t('lang.en')}
                    </button>
                  </div>
                )}
              </div>
            </div>

            {/* Burger mobile */}
            <button className="md:hidden p-2 text-gray-300" onClick={() => setOpen(!open)}>
              {open ? <X size={22} /> : <Menu size={22} />}
            </button>
          </div>
        </div>

        {/* Menu mobile */}
        {open && (
          <div className="md:hidden bg-[#1a1a1a] border-t border-[#2a2a2a] px-4 py-4 flex flex-col gap-2">
            {links.map(l => (
              <Link key={l.to} to={l.to} onClick={() => setOpen(false)}
                className={clsx('px-3 py-2 rounded text-sm font-medium',
                  isActive(l.to) ? 'text-red-500 bg-white/5' : 'text-gray-300')}>
                {l.label}
              </Link>
            ))}
            <div className="border-t border-[#2a2a2a] my-2" />
            {isAuthenticated ? (
              <>
                <Link to="/mon-compte" onClick={() => setOpen(false)}
                  className="px-3 py-2 text-sm text-gray-300">{t('nav.mon_compte')}</Link>
                <button onClick={handleLogout}
                  className="px-3 py-2 text-sm text-left text-red-500">{t('nav.deconnecter')}</button>
              </>
            ) : (
              <>
                <Link to="/login" onClick={() => setOpen(false)}
                  className="px-3 py-2 text-sm text-gray-300">{t('nav.connexion')}</Link>
                <Link to="/register" onClick={() => setOpen(false)}
                  className="px-3 py-2 text-sm text-red-500 font-medium">{t('nav.inscrire')}</Link>
              </>
            )}
            <div className="border-t border-[#2a2a2a] my-1 pt-2 flex gap-2 px-3">
              <button onClick={() => setLang('fr')}
                className={clsx('text-xs px-2 py-1 rounded border', i18n.language === 'fr' ? 'border-red-600 text-red-500' : 'border-[#2a2a2a] text-gray-400')}>
                🇫🇷 FR
              </button>
              <button onClick={() => setLang('en')}
                className={clsx('text-xs px-2 py-1 rounded border', i18n.language === 'en' ? 'border-red-600 text-red-500' : 'border-[#2a2a2a] text-gray-400')}>
                🇬🇧 EN
              </button>
            </div>
          </div>
        )}
      </nav>
    </>
  )
}
