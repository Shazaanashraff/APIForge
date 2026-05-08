import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { downloadJson } from './download'

describe('downloadJson', () => {
  let createObjectURL: ReturnType<typeof vi.fn>
  let revokeObjectURL: ReturnType<typeof vi.fn>
  let clickSpy: ReturnType<typeof vi.fn>

  beforeEach(() => {
    createObjectURL = vi.fn(() => 'blob:mock')
    revokeObjectURL = vi.fn()
    clickSpy = vi.fn()
    vi.stubGlobal('URL', { createObjectURL, revokeObjectURL })
    vi.spyOn(document, 'createElement').mockReturnValue({
      href: '',
      download: '',
      click: clickSpy,
    } as unknown as HTMLAnchorElement)
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  it('calls createObjectURL with a Blob', () => {
    downloadJson({ key: 'value' }, 'test.json')
    expect(createObjectURL).toHaveBeenCalledWith(expect.any(Blob))
  })

  it('triggers a click on the anchor element', () => {
    downloadJson({ x: 1 }, 'out.json')
    expect(clickSpy).toHaveBeenCalled()
  })

  it('revokes the object URL after click', () => {
    downloadJson({}, 'empty.json')
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:mock')
  })
})
