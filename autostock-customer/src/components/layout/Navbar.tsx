import { Link, useNavigate, useLocation } from 'react-router-dom'
import { Menu, X, LogOut, User, Phone } from 'lucide-react'
import { useState } from 'react'
import { useAuthStore } from '@/store/authStore'
import { clsx } from 'clsx'

export function Navbar() {
  const [open, setOpen] = useState(false)
  const { isAuthenticated, client, logout } = useAuthStore()
  const navigate = useNavigate()
  const location = useLocation()

  const handleLogout = () => { logout(); navigate('/') }

  const links = [
    { to: '/',          label: 'Accueil' },
    { to: '/catalogue', label: 'Catalogue' },
    { to: '/contact',   label: 'Contact' },
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
                <div className="text-red-600 text-[10px] tracking-widest uppercase">Vente de voitures usagées</div>
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

            {/* Auth desktop */}
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
                    Connexion
                  </Link>
                  <Link to="/register"
                    className="bg-red-600 hover:bg-red-700 text-white text-sm font-medium px-4 py-2 rounded transition-colors">
                    S'inscrire
                  </Link>
                </>
              )}
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
                  className="px-3 py-2 text-sm text-gray-300">Mon compte</Link>
                <button onClick={handleLogout}
                  className="px-3 py-2 text-sm text-left text-red-500">Se déconnecter</button>
              </>
            ) : (
              <>
                <Link to="/login" onClick={() => setOpen(false)}
                  className="px-3 py-2 text-sm text-gray-300">Connexion</Link>
                <Link to="/register" onClick={() => setOpen(false)}
                  className="px-3 py-2 text-sm text-red-500 font-medium">S'inscrire</Link>
              </>
            )}
          </div>
        )}
      </nav>
    </>
  )
}
