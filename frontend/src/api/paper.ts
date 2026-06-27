import { requestData } from './request'

import type { PaperGenerateRequest, PaperGenerateResponse } from '@/types/paper'

export function generatePaper(data: PaperGenerateRequest): Promise<PaperGenerateResponse> {
  return requestData<PaperGenerateResponse>({
    url: '/papers/generate',
    method: 'POST',
    data,
  })
}
