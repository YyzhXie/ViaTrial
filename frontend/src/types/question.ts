import type { Tag } from './tag'

export interface Question {
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
  createdTime: string
}

export interface QuestionAddRequest {
  subjectId: number
  typeId: number
  content: string
  answer?: string | null
  analysis?: string | null
  imageUrl?: string | null
  answerImageUrl?: string | null
  difficulty: number
  tagIds?: number[]
}

export interface QuestionPageRequest {
  page: number
  size: number
  subjectId?: number
  typeId?: number
  tagId?: number
}
