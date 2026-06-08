import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation } from '@tanstack/react-query'
import { Phone, Mail, MapPin, Clock, CheckCircle } from 'lucide-react'
import { contactApi } from '@/api/contact'
import { useSearchParams } from 'react-router-dom'

const schema = z.object({
  nom: z.string().min(2, 'Nom requis'),
  email: z.string().email('Email invalide'),
  telephone: z.string().optional(),
  sujet: z.string().min(3, 'Sujet requis'),
  message: z.string().min(10, 'Message trop court'),
})
type FormData = z.infer<typeof schema>

const inputClass = "w-full bg-[#1a1a1a] border border-[#2a2a2a] focus:border-red-600 text-white placeholder-gray-600 rounded-lg px-3 py-2.5 text-sm focus:outline-none transition-colors"

export default function Contact() {
  const [params] = useSearchParams()
  const vehiculeId = params.get('vehiculeId')

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      sujet: vehiculeId ? `Demande d'information — Véhicule #${vehiculeId}` : '',
    },
  })

  const { mutate, isPending, isSuccess } = useMutation({
    mutationFn: (data: FormData) => contactApi.envoyer(data),
    onSuccess: () => reset(),
  })

  const infos = [
    { icon: Phone,   label: 'Téléphone',  value: '+1 (438) 507-2586',    href: 'tel:+14385072586' },
    { icon: Mail,    label: 'Email',      value: 'contact@tedauto.com',   href: 'mailto:contact@tedauto.com' },
    { icon: MapPin,  label: 'Adresse',    value: 'Votre ville, QC, Canada', href: undefined },
    { icon: Clock,   label: 'Horaires',   value: 'Lun–Sam : 9h–18h',     href: undefined },
  ]

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-12">

      {/* En-tête */}
      <div className="mb-12 text-center">
        <p className="text-red-500 text-sm font-medium tracking-widest uppercase mb-2">On est là pour vous</p>
        <h1 className="text-4xl font-black text-white">
          NOUS <span className="text-red-600">CONTACTER</span>
        </h1>
        <div className="flex items-center justify-center gap-3 mt-4">
          <div className="h-px w-16 bg-red-600" />
          <div className="w-2 h-2 bg-red-600 rotate-45" />
          <div className="h-px w-16 bg-red-600" />
        </div>
      </div>

      <div className="grid md:grid-cols-5 gap-10">

        {/* Infos de contact */}
        <div className="md:col-span-2 flex flex-col gap-5">
          <div className="bg-[#111] border border-[#2a2a2a] rounded-xl p-6">
            <h2 className="text-white font-bold mb-5">Nos coordonnées</h2>
            <ul className="flex flex-col gap-4">
              {infos.map(({ icon: Icon, label, value, href }) => (
                <li key={label} className="flex items-start gap-3">
                  <div className="w-9 h-9 bg-red-600/10 border border-red-600/20 rounded-lg flex items-center justify-center shrink-0 mt-0.5">
                    <Icon size={15} className="text-red-500" />
                  </div>
                  <div>
                    <p className="text-gray-500 text-xs uppercase tracking-wider">{label}</p>
                    {href ? (
                      <a href={href} className="text-white hover:text-red-400 text-sm font-medium transition-colors">
                        {value}
                      </a>
                    ) : (
                      <p className="text-white text-sm font-medium">{value}</p>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          </div>

          <div className="bg-red-600 rounded-xl p-6">
            <p className="text-white font-bold text-lg mb-2">Réponse rapide</p>
            <p className="text-red-100 text-sm leading-relaxed">
              Notre équipe s'engage à vous répondre dans les 24h ouvrables. Pour une réponse immédiate, appelez-nous directement.
            </p>
          </div>
        </div>

        {/* Formulaire */}
        <div className="md:col-span-3">
          <div className="bg-[#111] border border-[#2a2a2a] rounded-xl p-6">
            <h2 className="text-white font-bold mb-5">Envoyer un message</h2>

            {isSuccess && (
              <div className="flex items-center gap-3 bg-green-900/30 border border-green-800/50 text-green-400 text-sm px-4 py-3 rounded-lg mb-5">
                <CheckCircle size={18} />
                Message envoyé ! Nous vous répondrons très prochainement.
              </div>
            )}

            <form onSubmit={handleSubmit(d => mutate(d))} className="flex flex-col gap-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-gray-400 text-xs uppercase tracking-wider mb-1.5 block">Nom complet</label>
                  <input {...register('nom')} placeholder="Jean Dupont" className={inputClass} />
                  {errors.nom && <p className="text-red-500 text-xs mt-1">{errors.nom.message}</p>}
                </div>
                <div>
                  <label className="text-gray-400 text-xs uppercase tracking-wider mb-1.5 block">Téléphone</label>
                  <input {...register('telephone')} type="tel" placeholder="+1 (514) 000-0000" className={inputClass} />
                </div>
              </div>

              <div>
                <label className="text-gray-400 text-xs uppercase tracking-wider mb-1.5 block">Email</label>
                <input {...register('email')} type="email" placeholder="jean@exemple.com" className={inputClass} />
                {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>}
              </div>

              <div>
                <label className="text-gray-400 text-xs uppercase tracking-wider mb-1.5 block">Sujet</label>
                <input {...register('sujet')} placeholder="Objet de votre message" className={inputClass} />
                {errors.sujet && <p className="text-red-500 text-xs mt-1">{errors.sujet.message}</p>}
              </div>

              <div>
                <label className="text-gray-400 text-xs uppercase tracking-wider mb-1.5 block">Message</label>
                <textarea {...register('message')} rows={5}
                  placeholder="Décrivez votre demande en détail…"
                  className={`${inputClass} resize-none`} />
                {errors.message && <p className="text-red-500 text-xs mt-1">{errors.message.message}</p>}
              </div>

              <button type="submit" disabled={isPending}
                className="w-full bg-red-600 hover:bg-red-700 disabled:opacity-50 text-white font-bold py-3 rounded-lg transition-colors">
                {isPending ? 'Envoi en cours…' : 'Envoyer le message'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}
