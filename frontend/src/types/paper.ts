import type { Tag } from './tag'

export interface PaperQuestion {
  id: number
  subjectId: number
  subjectName: string
  typeId: number
  typeName: string
  content: string
  answer?: string | null
  analysis?: string | null
  imageUrl?: string | null
  answerImageUrl?: string | null
  difficulty: number
  tags: Tag[]
}

export interface PaperGenerateRequest {
  subjectId: number
  typeCountMap: Record<number, number>
}

export interface PaperGenerateResponse {
  paperId: string
  totalRequested: number
  totalActual: number
  warnings: string[]
  questions: PaperQuestion[]
}
