import { useState } from 'react'
import { useProjectStore } from '@/store/projectStore'
import { ProjectCard } from '@/components/project/ProjectCard'
import { CreateProjectModal } from '@/components/project/CreateProjectModal'

export default function ProjectsPage() {
  const { projects, add, update } = useProjectStore()
  const [showModal, setShowModal] = useState(false)
  const [filter, setFilter] = useState('')

  const filtered = filter
    ? projects.filter((p) =>
        p.name.toLowerCase().includes(filter.toLowerCase()),
      )
    : projects

  return (
    <div className="space-y-6 max-w-5xl">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold mb-1">Projects</h1>
          <p className="text-gray-400 text-sm">
            Manage API projects and their test configuration.
          </p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="shrink-0 px-4 py-2 bg-brand-600 hover:bg-brand-500 rounded-lg text-sm font-medium transition-colors"
        >
          + New Project
        </button>
      </div>

      <input
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
        placeholder="Filter projects…"
        className="w-full bg-gray-900 border border-gray-800 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-brand-500"
      />

      {filtered.length === 0 ? (
        <div className="text-center py-16 text-gray-500 text-sm">
          {projects.length === 0
            ? 'No projects yet. Create one to get started.'
            : 'No projects match your filter.'}
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((project) => (
            <ProjectCard
              key={project.id}
              project={project}
              onToggleMongo={(id, enabled) =>
                update(id, { mongoEnabled: enabled })
              }
            />
          ))}
        </div>
      )}

      {showModal && (
        <CreateProjectModal
          onClose={() => setShowModal(false)}
          onCreate={(input) => add(input)}
        />
      )}
    </div>
  )
}
