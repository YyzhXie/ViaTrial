import { requestData } from './request'

import type { PageResult } from '@/types/common'
import type { Question, QuestionAddRequest, QuestionPageRequest } from '@/types/question'

export function addQuestion(data: QuestionAddRequest): Promise<number> {
  return requestData<number>({
    url: '/questions',
    method: 'POST',
    data,
  })
}

export function updateQuestion(id: number, data: QuestionAddRequest): Promise<boolean> {
  return requestData<boolean>({
    url: `/questions/${id}`,
    method: 'PUT',
    data,
  })
}

export function pageQuestions(params: QuestionPageRequest): Promise<PageResult<Question>> {
  return requestData<PageResult<Question>>({
    url: '/questions/page',
    method: 'GET',
    params,
  })
}

export function deleteQuestion(id: number): Promise<boolean> {
  return requestData<boolean>({
    url: `/questions/${id}`,
    method: 'DELETE',
  })
}
