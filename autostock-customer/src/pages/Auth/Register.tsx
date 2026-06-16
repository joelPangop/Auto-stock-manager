import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import { useTranslation } from 'react-i18next'

const schema = z.object({
  nom: z.string().min(2, 'Nom requis (min. 2 caractères)'),
  email: z.string().email('Email invalide'),
  telephone: z.string().optional(),
  motDePasse: z.string().min(6, 'Minimum 6 caractères'),
  confirmMotDePasse: z.string(),
}).refine(d => d.motDePasse === d.confirmMotDePasse, {
  message: 'Les mots de passe ne correspondent pas',
  path: ['confirmMotDePasse'],
})
type FormData = z.infer<typeof schema>

const Field = ({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) => (
  <div>
    <label className="text-gray-400 text-xs uppercase tracking-wider mb-1.5 block">{label}</label>
    {children}
    {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
  </div>
)

const inputClass = "w-full bg-[#1a1a1a] border border-[#2a2a2a] focus:border-red-600 text-white placeholder-gray-600 rounded-lg px-3 py-2.5 text-sm focus:outline-none transition-colors"

export default function Register() {
  const navigate = useNavigate()
  const { login } = useAuthStore()
  const { t } = useTranslation()
  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({ resolver: zodResolver(schema) })

  const { mutate, isPending, error } = useMutation({
    mutationFn: ({ confirmMotDePasse: _, ...data }: FormData) => authApi.register(data),
    onSuccess: ({ token, client }) => { login(token, client); navigate('/mon-compte') },
  })

  return (
    <div className="min-h-[calc(100vh-200px)] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <img src="/assets/logo.jpg" alt="Ted Auto" className="h-16 mx-auto object-contain mb-4"
            onError={e => { (e.target as HTMLImageElement).style.display = 'none' }} />
          <h1 className="text-2xl font-black text-white">{t('auth.creer_compte')}</h1>
          <p className="text-gray-500 text-sm mt-1">{t('auth.acces_espace_ted')}</p>
        </div>

        <div className="bg-[#111] border border-[#2a2a2a] rounded-xl p-6">
          {error && (
            <div className="bg-red-900/30 border border-red-800/50 text-red-400 text-sm px-4 py-3 rounded-lg mb-4">
              {t('auth.erreur_register')}
            </div>
          )}

          <form onSubmit={handleSubmit(d => mutate(d))} className="flex flex-col gap-4">
            <Field label={t('auth.nom_complet')} error={errors.nom?.message}>
              <input {...register('nom')} placeholder="Jean Dupont" className={inputClass} />
            </Field>
            <Field label="Email" error={errors.email?.message}>
              <input {...register('email')} type="email" placeholder="jean@exemple.com" className={inputClass} />
            </Field>
            <Field label={t('auth.telephone_opt')} error={errors.telephone?.message}>
              <input {...register('telephone')} type="tel" placeholder="+1 (514) 000-0000" className={inputClass} />
            </Field>
            <Field label={t('auth.mot_de_passe')} error={errors.motDePasse?.message}>
              <input {...register('motDePasse')} type="password" placeholder={t('auth.min_6')} className={inputClass} />
            </Field>
            <Field label={t('auth.confirmer_mdp')} error={errors.confirmMotDePasse?.message}>
              <input {...register('confirmMotDePasse')} type="password" placeholder="••••••••" className={inputClass} />
            </Field>

            <button type="submit" disabled={isPending}
              className="w-full bg-red-600 hover:bg-red-700 disabled:opacity-50 text-white font-bold py-3 rounded-lg transition-colors mt-2">
              {isPending ? t('auth.creation_en_cours') : t('auth.creer_mon_compte')}
            </button>
          </form>
        </div>

        <p className="text-center text-gray-500 text-sm mt-5">
          {t('auth.deja_inscrit')}{' '}
          <Link to="/login" className="text-red-500 hover:text-red-400 font-medium">{t('auth.se_connecter')}</Link>
        </p>
      </div>
    </div>
  )
}
