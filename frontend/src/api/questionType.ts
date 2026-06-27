import { requestData } from './request'

import type { QuestionType, QuestionTypeAddRequest } from '@/types/questionType'

export function addQuestionType(data: QuestionTypeAddRequest): Promise<number> {
  return requestData<number>({
    url: '/question-types',
    method: 'POST',
    data,
  })
}

export function listQuestionTypes(subjectId: number): Promise<QuestionType[]> {
  return requestData<QuestionType[]>({
    url: '/question-types',
    method: 'GET',
    params: { subjectId },
  })
}

export function deleteQuestionType(id: number): Promise<boolean> {
  return requestData<boolean>({
    url: `/question-types/${id}`,
    method: 'DELETE',
  })
}
