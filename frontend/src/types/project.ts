export interface Project {
  id: string
  name: string
  description: string
  specUrl: string
  mongoEnabled: boolean
  createdAt: string
}

export type CreateProjectInput = Omit<Project, 'id' | 'createdAt'>
