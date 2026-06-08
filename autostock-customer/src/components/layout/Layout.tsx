import { Outlet } from 'react-router-dom'
import { Navbar } from './Navbar'
import { Phone, Mail, MapPin } from 'lucide-react'
import { Link } from 'react-router-dom'

export function Layout() {
  return (
    <div className="min-h-screen flex flex-col bg-[#0a0a0a]">
      <Navbar />
      <main className="flex-1">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-[#111] border-t border-[#2a2a2a] mt-20">
        <div className="max-w-7xl mx-auto px-6 py-12 grid grid-cols-1 md:grid-cols-3 gap-10">
          {/* Logo + slogan */}
          <div className="flex flex-col gap-4">
            <div>
              <img src="/assets/logo.jpg" alt="Ted Auto" className="h-12 w-auto object-contain"
                onError={e => { (e.target as HTMLImageElement).style.display = 'none' }} />
              <p className="text-gray-400 text-sm mt-3 leading-relaxed">
                Votre confiance, notre priorité.<br />
                Spécialiste de la vente de voitures usagées depuis 2015.
              </p>
            </div>
            <div className="w-16 h-0.5 bg-red-600" />
          </div>

          {/* Liens */}
          <div>
            <h4 className="text-white font-semibold mb-4 uppercase tracking-wider text-sm">Navigation</h4>
            <ul className="flex flex-col gap-2">
              {[['/', 'Accueil'], ['/catalogue', 'Catalogue'], ['/contact', 'Contact'],
                ['/login', 'Connexion'], ['/register', 'S\'inscrire']].map(([to, label]) => (
                <li key={to}>
                  <Link to={to} className="text-gray-400 hover:text-red-500 text-sm transition-colors">
                    {label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h4 className="text-white font-semibold mb-4 uppercase tracking-wider text-sm">Contact</h4>
            <ul className="flex flex-col gap-3">
              <li className="flex items-center gap-2 text-gray-400 text-sm">
                <Phone size={14} className="text-red-500 shrink-0" />
                +1 (438) 507-2586
              </li>
              <li className="flex items-center gap-2 text-gray-400 text-sm">
                <Mail size={14} className="text-red-500 shrink-0" />
                contact@tedauto.com
              </li>
              <li className="flex items-start gap-2 text-gray-400 text-sm">
                <MapPin size={14} className="text-red-500 shrink-0 mt-0.5" />
                Votre ville, QC, Canada
              </li>
            </ul>
          </div>
        </div>

        <div className="border-t border-[#2a2a2a] py-4">
          <p className="text-center text-gray-600 text-xs">
            © {new Date().getFullYear()} Ted Auto — Tous droits réservés
          </p>
        </div>
      </footer>
    </div>
  )
}
