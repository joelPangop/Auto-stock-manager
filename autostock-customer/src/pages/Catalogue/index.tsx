import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Search, SlidersHorizontal, X } from 'lucide-react'
import { catalogueApi } from '@/api/catalogue'
import { VehicleCard } from '@/components/VehicleCard'
import type { FiltresCatalogue } from '@/types'
import { useTranslation } from 'react-i18next'

export default function Catalogue() {
  const [filtres, setFiltres] = useState<FiltresCatalogue>({ page: 0, size: 12 })
  const [search, setSearch] = useState('')
  const [showFiltres, setShowFiltres] = useState(false)
  const { t } = useTranslation()

  const { data, isLoading } = useQuery({
    queryKey: ['vehicules', filtres, search],
    queryFn: () => catalogueApi.list({ ...filtres, search: search.trim() || undefined }),
  })

  const { data: marques } = useQuery({
    queryKey: ['marques'],
    queryFn: catalogueApi.getMarques,
  })

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setFiltres(f => ({ ...f, page: 0 }))
  }

  const total = data?.totalElements ?? 0

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">

      <div className="mb-8">
        <p className="text-red-500 text-sm font-medium tracking-widest uppercase mb-1">{t('catalogue.sous_titre')}</p>
        <h1 className="text-3xl font-black text-white">
          {t('catalogue.titre')} <span className="text-red-600">{t('catalogue.titre_accent')}</span>
        </h1>
        <div className="w-12 h-0.5 bg-red-600 mt-3" />
      </div>

      <form onSubmit={handleSearch} className="flex flex-wrap gap-3 mb-6">
        <div className="flex-1 relative">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
          <input
            type="text"
            placeholder={t('catalogue.rechercher')}
            value={search}
            onChange={e => { setSearch(e.target.value); setFiltres(f => ({ ...f, page: 0 })) }}
            className="w-full pl-9 pr-4 py-2.5 bg-[#1a1a1a] border border-[#2a2a2a] text-white placeholder-gray-600 rounded focus:outline-none focus:border-red-600 text-sm transition-colors"
          />
          {search && (
            <button type="button" onClick={() => setSearch('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-white">
              <X size={14} />
            </button>
          )}
        </div>
        <button type="button"
          onClick={() => setShowFiltres(!showFiltres)}
          className="flex items-center gap-2 px-4 py-2.5 bg-[#1a1a1a] border border-[#2a2a2a] text-gray-300 hover:border-red-600 rounded text-sm transition-colors">
          <SlidersHorizontal size={16} />
          {t('catalogue.filtres')}
        </button>
        <button type="submit"
          className="px-5 py-2.5 bg-red-600 hover:bg-red-700 text-white font-medium rounded text-sm transition-colors">
          {t('catalogue.bouton_rechercher')}
        </button>
      </form>

      {showFiltres && (
        <div className="bg-[#111] border border-[#2a2a2a] rounded-lg p-5 mb-6 grid grid-cols-2 md:grid-cols-4 gap-4">
          <div>
            <label className="text-gray-500 text-xs uppercase tracking-wider mb-1.5 block">{t('catalogue.marque')}</label>
            <select value={filtres.marque ?? ''}
              onChange={e => setFiltres(f => ({ ...f, marque: e.target.value || undefined, page: 0 }))}
              className="w-full bg-[#1a1a1a] border border-[#2a2a2a] text-white rounded px-3 py-2 text-sm focus:outline-none focus:border-red-600">
              <option value="">{t('catalogue.toutes')}</option>
              {marques?.map(m => <option key={m} value={m}>{m}</option>)}
            </select>
          </div>
          <div>
            <label className="text-gray-500 text-xs uppercase tracking-wider mb-1.5 block">{t('catalogue.prix_max')}</label>
            <input type="number" placeholder="Ex: 25000"
              value={filtres.prixMax ?? ''}
              onChange={e => setFiltres(f => ({ ...f, prixMax: e.target.value ? +e.target.value : undefined, page: 0 }))}
              className="w-full bg-[#1a1a1a] border border-[#2a2a2a] text-white rounded px-3 py-2 text-sm focus:outline-none focus:border-red-600" />
          </div>
          <div>
            <label className="text-gray-500 text-xs uppercase tracking-wider mb-1.5 block">{t('catalogue.annee_min')}</label>
            <input type="number" placeholder="Ex: 2018"
              value={filtres.anneeMin ?? ''}
              onChange={e => setFiltres(f => ({ ...f, anneeMin: e.target.value ? +e.target.value : undefined, page: 0 }))}
              className="w-full bg-[#1a1a1a] border border-[#2a2a2a] text-white rounded px-3 py-2 text-sm focus:outline-none focus:border-red-600" />
          </div>
          <div className="flex items-end">
            <button onClick={() => { setFiltres({ page: 0, size: 12 }); setSearch('') }}
              className="w-full py-2 border border-[#2a2a2a] hover:border-red-600 text-gray-400 hover:text-red-500 rounded text-sm transition-colors">
              {t('catalogue.reinitialiser')}
            </button>
          </div>
        </div>
      )}

      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="bg-[#111] border border-[#2a2a2a] rounded-lg overflow-hidden animate-pulse">
              <div className="aspect-[16/10] bg-[#1a1a1a]" />
              <div className="p-4 flex flex-col gap-2">
                <div className="h-3 bg-[#2a2a2a] rounded w-1/3" />
                <div className="h-4 bg-[#2a2a2a] rounded w-2/3" />
                <div className="h-3 bg-[#2a2a2a] rounded w-full mt-2" />
              </div>
            </div>
          ))}
        </div>
      ) : data?.content.length === 0 ? (
        <div className="text-center py-20 text-gray-600">
          <p className="text-5xl mb-4">🚗</p>
          <p className="text-lg font-medium text-gray-500">{t('catalogue.aucun_titre')}</p>
          <p className="text-sm mt-1">{t('catalogue.aucun_desc')}</p>
        </div>
      ) : (
        <>
          <p className="text-gray-500 text-sm mb-4">
            {total} {total > 1 ? t('catalogue.vehicule_pluriel') : t('catalogue.vehicule')} {total > 1 ? t('catalogue.trouves') : t('catalogue.trouve')}
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
            {data?.content.map(v => <VehicleCard key={v.id} vehicule={v} />)}
          </div>

          {(data?.totalPages ?? 0) > 1 && (
            <div className="flex justify-center gap-2 mt-10">
              {Array.from({ length: data!.totalPages }).map((_, i) => (
                <button key={i}
                  onClick={() => setFiltres(f => ({ ...f, page: i }))}
                  className={`w-9 h-9 rounded text-sm font-medium transition-colors ${
                    filtres.page === i
                      ? 'bg-red-600 text-white'
                      : 'bg-[#1a1a1a] border border-[#2a2a2a] text-gray-400 hover:border-red-600 hover:text-white'
                  }`}>
                  {i + 1}
                </button>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}
