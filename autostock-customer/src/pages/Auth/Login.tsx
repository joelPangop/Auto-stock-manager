import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import { useTranslation } from 'react-i18next'

const schema = z.object({
  email: z.string().email('Email invalide'),
  motDePasse: z.string().min(1, 'Mot de passe requis'),
})
type FormData = z.infer<typeof schema>

export default function Login() {
  const navigate = useNavigate()
  const { login } = useAuthStore()
  const { t } = useTranslation()
  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({ resolver: zodResolver(schema) })

  const { mutate, isPending, error } = useMutation({
    mutationFn: (data: FormData) => authApi.login(data),
    onSuccess: ({ token, client }) => { login(token, client); navigate('/mon-compte') },
  })

  return (
    <div className="min-h-[calc(100vh-200px)] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">

        <div className="text-center mb-8">
          <img src="/assets/logo.jpg" alt="Ted Auto" className="h-16 mx-auto object-contain mb-4"
            onError={e => { (e.target as HTMLImageElement).style.display = 'none' }} />
          <h1 className="text-2xl font-black text-white">{t('auth.connexion')}</h1>
          <p className="text-gray-500 text-sm mt-1">{t('auth.acces_espace')}</p>
        </div>

        <div className="bg-[#111] border border-[#2a2a2a] rounded-xl p-6">
          {error && (
            <div className="bg-red-900/30 border border-red-800/50 text-red-400 text-sm px-4 py-3 rounded-lg mb-4">
              {t('auth.erreur_login')}
            </div>
          )}

          <form onSubmit={handleSubmit(d => mutate(d))} className="flex flex-col gap-4">
            <div>
              <label className="text-gray-400 text-xs uppercase tracking-wider mb-1.5 block">Email</label>
              <input {...register('email')} type="email" placeholder="votre@email.com"
                className="w-full bg-[#1a1a1a] border border-[#2a2a2a] focus:border-red-600 text-white placeholder-gray-600 rounded-lg px-3 py-2.5 text-sm focus:outline-none transition-colors" />
              {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>}
            </div>

            <div>
              <label className="text-gray-400 text-xs uppercase tracking-wider mb-1.5 block">{t('auth.mot_de_passe')}</label>
              <input {...register('motDePasse')} type="password" placeholder="••••••••"
                className="w-full bg-[#1a1a1a] border border-[#2a2a2a] focus:border-red-600 text-white placeholder-gray-600 rounded-lg px-3 py-2.5 text-sm focus:outline-none transition-colors" />
              {errors.motDePasse && <p className="text-red-500 text-xs mt-1">{errors.motDePasse.message}</p>}
            </div>

            <button type="submit" disabled={isPending}
              className="w-full bg-red-600 hover:bg-red-700 disabled:opacity-50 text-white font-bold py-3 rounded-lg transition-colors mt-2">
              {isPending ? t('auth.connexion_en_cours') : t('auth.se_connecter')}
            </button>
          </form>
        </div>

        <p className="text-center text-gray-500 text-sm mt-5">
          {t('auth.pas_de_compte')}{' '}
          <Link to="/register" className="text-red-500 hover:text-red-400 font-medium">{t('auth.inscrire')}</Link>
        </p>
      </div>
    </div>
  )
}
