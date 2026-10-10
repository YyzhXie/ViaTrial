export type ProgrammingLanguage = 'c' | 'cpp' | 'java' | 'python'

export interface ProgrammingTestCase {
  input: string
  expectedOutput: string
  isSample: boolean
}

export interface ProgrammingQuestionData {
  statement: string
  language: ProgrammingLanguage
  starterCode: string
  testCases: ProgrammingTestCase[]
  competitionMode: boolean
  timeLimitMs: number
  memoryLimitMb: number
}

const MARKER = '<!--viatrial-programming:v2:'
export const MAX_PROGRAMMING_QUESTION_LENGTH = 750000

export function encodeProgrammingQuestion(data: ProgrammingQuestionData): string {
  const normalized: ProgrammingQuestionData = {
    ...data,
    competitionMode: Boolean(data.competitionMode),
    timeLimitMs: data.timeLimitMs || 1000,
    memoryLimitMb: data.memoryLimitMb || 256,
  }
  const bytes = new TextEncoder().encode(JSON.stringify(normalized))
  const chunks: string[] = []
  for (let offset = 0; offset < bytes.length; offset += 0x8000) {
    chunks.push(String.fromCharCode(...bytes.subarray(offset, offset + 0x8000)))
  }
  const encoded = btoa(chunks.join(''))
  return `${MARKER}${encoded}-->\n${data.statement.trim()}`
}

export function decodeProgrammingQuestion(content: string): ProgrammingQuestionData | null {
  const match = content.match(/<!--viatrial-programming:v([12]):([^>]*)-->/)
  if (!match) return null
  try {
    const bytes = Uint8Array.from(atob(match[2]), (character) => character.charCodeAt(0))
    const data = JSON.parse(new TextDecoder().decode(bytes)) as ProgrammingQuestionData
    if (!['c', 'cpp', 'java', 'python'].includes(data.language)) return null
    return {
      statement: content.replace(match[0], '').trim(),
      language: data.language,
      starterCode: typeof data.starterCode === 'string' ? data.starterCode : '',
      competitionMode: data.competitionMode === true,
      timeLimitMs: Number.isInteger(data.timeLimitMs) ? data.timeLimitMs : 1000,
      memoryLimitMb: Number.isInteger(data.memoryLimitMb) ? data.memoryLimitMb : 256,
      testCases: Array.isArray(data.testCases)
        ? data.testCases
          .filter((item) => item && typeof item.input === 'string' && typeof item.expectedOutput === 'string')
          .map((item) => ({ ...item, isSample: typeof item.isSample === 'boolean' ? item.isSample : true }))
        : [],
    }
  } catch {
    return null
  }
}

export const isProgrammingQuestion = (typeName: string) => /编程|程序|programming|coding/i.test(typeName)

export const languageLabels: Record<ProgrammingLanguage, string> = {
  c: 'C',
  cpp: 'C++',
  java: 'Java',
  python: 'Python 3',
}

