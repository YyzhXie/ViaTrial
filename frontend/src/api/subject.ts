import { requestData } from './request'

import type { Subject, SubjectAddRequest } from '@/types/subject'

export function addSubject(data: SubjectAddRequest): Promise<number> {
  return requestData<number>({
    url: '/subjects',
    method: 'POST',
    data,
  })
}

export function listSubjects(): Promise<Subject[]> {
  return requestData<Subject[]>({
    url: '/subjects',
    method: 'GET',
  })
}

export function deleteSubject(id: number): Promise<boolean> {
  return requestData<boolean>({
    url: `/subjects/${id}`,
    method: 'DELETE',
  })
}
