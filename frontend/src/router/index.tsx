import { createBrowserRouter } from 'react-router-dom'
import { Shell } from '@/components/layout/Shell'
import { ProtectedRoute } from '@/components/auth/ProtectedRoute'
import DashboardPage from '@/pages/DashboardPage'
import NotFoundPage from '@/pages/NotFoundPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <Shell />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'projects', element: <div className="text-gray-400">Projects — coming in S17</div> },
      { path: 'specs', element: <div className="text-gray-400">API Specs — coming in S17</div> },
      { path: 'runs', element: <div className="text-gray-400">Test Runs — coming in S18</div> },
      { path: 'reports', element: <div className="text-gray-400">Reports — coming in S19</div> },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
])
