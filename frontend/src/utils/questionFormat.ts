export interface QuestionOption {
  text: string
}

export interface StructuredQuestion {
  stem: string
  options: QuestionOption[]
  multiple: boolean
}

const CONTENT_MARKER = '<!--viatrial-question:v1:'
const ANSWER_MARKER = '[[viatrial-answers:v1]]'
export type FillAnswerMode = 'alternatives' | 'blanks'

export interface FillAnswerData {
  values: string[]
  mode: FillAnswerMode
}

export function encodeQuestionContent(question: StructuredQuestion): string {
  const payload = encodeURIComponent(JSON.stringify({
    options: question.options.map((option) => option.text),
    multiple: question.multiple,
  }))
  return `${question.stem.trim()}\n\n${CONTENT_MARKER}${payload}-->`
}

export function decodeQuestionContent(content: string): StructuredQuestion {
  const marker = content.match(/<!--viatrial-question:v1:([^>]*)-->/)
  if (marker) {
    try {
      const parsed = JSON.parse(decodeURIComponent(marker[1])) as { options?: string[]; multiple?: boolean }
      return {
        stem: content.replace(marker[0], '').trim(),
        options: Array.isArray(parsed.options) ? parsed.options.map((text) => ({ text })) : [],
        multiple: Boolean(parsed.multiple),
      }
    } catch {
      // Fall through to the legacy plain-text option format.
    }
  }

  const options: QuestionOption[] = []
  const stem: string[] = []
  const optionPattern = /^\s*([A-Z])(?:[.、．:：)）]|\s+)\s*(.*)$/i
  content.split(/\r?\n/).forEach((line) => {
    const match = line.match(optionPattern)
    if (match) options.push({ text: match[2].trim() })
    else stem.push(line)
  })
  return { stem: stem.join('\n').trim(), options, multiple: false }
}

export function encodeAnswers(answers: string[], mode: FillAnswerMode = 'alternatives'): string {
  return `${ANSWER_MARKER}${JSON.stringify({ values: answers, mode })}`
}

export function decodeAnswerData(answer?: string | null): FillAnswerData {
  if (!answer) return { values: [''], mode: 'alternatives' }
  if (answer.startsWith(ANSWER_MARKER)) {
    try {
      const parsed = JSON.parse(answer.slice(ANSWER_MARKER.length))
      if (Array.isArray(parsed)) return { values: parsed.map((value) => String(value ?? '')), mode: 'alternatives' }
      if (Array.isArray(parsed.values)) {
        return {
          values: parsed.values.map((value: unknown) => String(value ?? '')),
          mode: parsed.mode === 'blanks' ? 'blanks' : 'alternatives',
        }
      }
    } catch {
      // Treat malformed data as a regular legacy answer.
    }
  }
  return { values: answer.split(/\r?\n/), mode: 'alternatives' }
}

export function decodeAnswers(answer?: string | null): string[] {
  return decodeAnswerData(answer).values
}

export function isChoiceType(typeName: string): boolean {
  return /选择|单选|多选|choice|multiple/i.test(typeName)
}
