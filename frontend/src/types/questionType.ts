export interface QuestionType {
  id: number
  subjectId: number
  name: string
}

export interface QuestionTypeAddRequest {
  subjectId: number
  name: string
}
