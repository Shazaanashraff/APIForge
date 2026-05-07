import type { Project } from '@/types/project'
import { useProjectStore } from '@/store/projectStore'

interface Props {
  project: Project
  onToggleMongo: (id: string, enabled: boolean) => void
}

export function ProjectCard({ project, onToggleMongo }: Props) {
  const remove = useProjectStore((s) => s.remove)

  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5 space-y-3">
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0">
          <h3 className="font-semibold truncate">{project.name}</h3>
          {project.description && (
            <p className="text-xs text-gray-400 mt-0.5 line-clamp-2">
              {project.description}
            </p>
          )}
        </div>
        <button
          onClick={() => remove(project.id)}
          className="text-gray-600 hover:text-red-400 transition-colors text-lg leading-none shrink-0"
          aria-label="Delete project"
        >
          ×
        </button>
      </div>
      {project.specUrl && (
        <p className="text-xs font-mono text-gray-500 truncate">
          {project.specUrl}
        </p>
      )}
      <div className="flex items-center justify-between pt-1">
        <span className="text-xs text-gray-500">
          {new Date(project.createdAt).toLocaleDateString()}
        </span>
        <label className="flex items-center gap-2 cursor-pointer select-none">
          <span className="text-xs text-gray-400">MongoDB</span>
          <button
            role="switch"
            aria-checked={project.mongoEnabled}
            onClick={() => onToggleMongo(project.id, !project.mongoEnabled)}
            className={`relative w-8 h-4 rounded-full transition-colors ${
              project.mongoEnabled ? 'bg-brand-600' : 'bg-gray-700'
            }`}
          >
            <span
              className={`absolute top-0.5 w-3 h-3 bg-white rounded-full transition-transform ${
                project.mongoEnabled ? 'translate-x-4' : 'translate-x-0.5'
              }`}
            />
          </button>
        </label>
      </div>
    </div>
  )
}
