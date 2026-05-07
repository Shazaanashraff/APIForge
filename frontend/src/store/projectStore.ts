import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { Project, CreateProjectInput } from '@/types/project'

interface ProjectState {
  projects: Project[]
  add: (input: CreateProjectInput) => Project
  update: (id: string, patch: Partial<CreateProjectInput>) => void
  remove: (id: string) => void
}

export const useProjectStore = create<ProjectState>()(
  persist(
    (set, get) => ({
      projects: [],
      add: (input) => {
        const project: Project = {
          ...input,
          id: crypto.randomUUID(),
          createdAt: new Date().toISOString(),
        }
        set({ projects: [...get().projects, project] })
        return project
      },
      update: (id, patch) =>
        set({
          projects: get().projects.map((p) =>
            p.id === id ? { ...p, ...patch } : p,
          ),
        }),
      remove: (id) =>
        set({ projects: get().projects.filter((p) => p.id !== id) }),
    }),
    { name: 'apiforge-projects' },
  ),
)
