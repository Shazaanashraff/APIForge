import { createBrowserRouter } from 'react-router-dom'
import { Shell } from '@/components/layout/Shell'
import { ProtectedRoute } from '@/components/auth/ProtectedRoute'
import DashboardPage from '@/pages/DashboardPage'
import ProjectsPage from '@/pages/ProjectsPage'
import SpecsPage from '@/pages/SpecsPage'
import RunsPage from '@/pages/RunsPage'
import ReportsPage from '@/pages/ReportsPage'
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
      { path: 'projects', element: <ProjectsPage /> },
      { path: 'specs', element: <SpecsPage /> },
      { path: 'runs', element: <RunsPage /> },
      { path: 'reports', element: <ReportsPage /> },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
])
