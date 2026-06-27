import { requestData } from './request'

import type { Tag, TagAddRequest } from '@/types/tag'

export function addTag(data: TagAddRequest): Promise<number> {
  return requestData<number>({
    url: '/tags',
    method: 'POST',
    data,
  })
}

export function listTags(): Promise<Tag[]> {
  return requestData<Tag[]>({
    url: '/tags',
    method: 'GET',
  })
}

export function deleteTag(id: number): Promise<boolean> {
  return requestData<boolean>({
    url: `/tags/${id}`,
    method: 'DELETE',
  })
}
