import { describe, it, expect, beforeEach } from 'vitest'
import { useProjectStore } from './projectStore'

beforeEach(() => {
  useProjectStore.setState({ projects: [] })
})

describe('projectStore', () => {
  it('add creates a project with id and createdAt', () => {
    const p = useProjectStore.getState().add({
      name: 'Alpha',
      description: '',
      specUrl: '',
      mongoEnabled: false,
    })
    expect(p.id).toBeTruthy()
    expect(p.createdAt).toBeTruthy()
    expect(useProjectStore.getState().projects).toHaveLength(1)
  })

  it('update patches an existing project', () => {
    const p = useProjectStore.getState().add({
      name: 'Beta',
      description: '',
      specUrl: '',
      mongoEnabled: false,
    })
    useProjectStore.getState().update(p.id, { mongoEnabled: true })
    const updated = useProjectStore
      .getState()
      .projects.find((x) => x.id === p.id)
    expect(updated?.mongoEnabled).toBe(true)
  })

  it('remove deletes the project', () => {
    const p = useProjectStore.getState().add({
      name: 'Gamma',
      description: '',
      specUrl: '',
      mongoEnabled: false,
    })
    useProjectStore.getState().remove(p.id)
    expect(useProjectStore.getState().projects).toHaveLength(0)
  })
})
