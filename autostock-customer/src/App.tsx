import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Layout } from '@/components/layout/Layout'
import { useAuthStore } from '@/store/authStore'
import { lazy, Suspense } from 'react'

const Home          = lazy(() => import('@/pages/Home'))
const Catalogue     = lazy(() => import('@/pages/Catalogue'))
const VehicleDetail = lazy(() => import('@/pages/VehicleDetail'))
const Contact       = lazy(() => import('@/pages/Contact'))
const Login         = lazy(() => import('@/pages/Auth/Login'))
const Register      = lazy(() => import('@/pages/Auth/Register'))
const MonCompte     = lazy(() => import('@/pages/MonCompte'))

const qc = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 1000 * 60 } },
})

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore()
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />
}

function PageLoader() {
  return (
    <div className="flex items-center justify-center min-h-[400px]">
      <div className="w-8 h-8 border-2 border-[#2a2a2a] border-t-red-600 rounded-full animate-spin" />
    </div>
  )
}

export default function App() {
  return (
    <QueryClientProvider client={qc}>
      <BrowserRouter>
        <Suspense fallback={<PageLoader />}>
          <Routes>
            <Route element={<Layout />}>
              <Route path="/"              element={<Home />} />
              <Route path="/catalogue"     element={<Catalogue />} />
              <Route path="/catalogue/:id" element={<VehicleDetail />} />
              <Route path="/contact"       element={<Contact />} />
              <Route path="/login"         element={<Login />} />
              <Route path="/register"      element={<Register />} />
              <Route path="/mon-compte"    element={<PrivateRoute><MonCompte /></PrivateRoute>} />
              <Route path="*"              element={<Navigate to="/" replace />} />
            </Route>
          </Routes>
        </Suspense>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
