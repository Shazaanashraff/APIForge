import { NavLink } from 'react-router-dom'
import { clsx } from 'clsx'

const links = [
  { to: '/', label: 'Dashboard', icon: '🏠' },
  { to: '/projects', label: 'Projects', icon: '📁' },
  { to: '/specs', label: 'API Specs', icon: '📄' },
  { to: '/runs', label: 'Test Runs', icon: '▶️' },
  { to: '/reports', label: 'Reports', icon: '📊' },
]

export function Sidebar() {
  return (
    <aside className="w-56 shrink-0 bg-gray-900 border-r border-gray-800 flex flex-col">
      <div className="px-5 py-4 border-b border-gray-800">
        <span className="text-lg font-bold text-brand-400 tracking-tight">
          APIForge
        </span>
      </div>
      <nav className="flex-1 py-4 space-y-1 px-2">
        {links.map(({ to, label, icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) =>
              clsx(
                'flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                isActive
                  ? 'bg-brand-600 text-white'
                  : 'text-gray-400 hover:bg-gray-800 hover:text-white',
              )
            }
          >
            <span>{icon}</span>
            {label}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}
