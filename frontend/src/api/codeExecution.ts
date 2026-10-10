import { requestData } from './request'

import type { ProgrammingLanguage } from '@/utils/programmingQuestion'

export interface CodeExecutionRequest {
  sourceCode: string
  language: ProgrammingLanguage
  input: string
  expectedOutput: string
  timeLimitMs?: number
  memoryLimitMb?: number
}

export interface CodeExecutionResult {
  verdict: 'AC' | 'WA' | 'TLE' | 'MLE' | 'CE' | 'RE' | 'ERROR'
  status: string
  stdout: string | null
  stderr: string | null
  compileOutput: string | null
  time: string | null
  memory: string | null
}

export interface CodeGradingResult {
  verdict: 'AC' | 'WA' | 'TLE' | 'MLE' | 'CE' | 'RE' | 'ERROR'
  status: string
  testPoints: Array<{
    index: number
    label: string
    verdict: 'AC' | 'WA' | 'TLE' | 'MLE' | 'CE' | 'RE' | 'ERROR'
    status: string
    time: string | null
    memory: string | null
  }>
}

export function executeCode(data: CodeExecutionRequest): Promise<CodeExecutionResult> {
  return requestData<CodeExecutionResult>({
    url: '/code/execute',
    method: 'POST',
    data,
    timeout: 300000,
  })
}

export function gradeCode(data: { questionId: number; sourceCode: string; language: ProgrammingLanguage }): Promise<CodeGradingResult> {
  return requestData<CodeGradingResult>({
    url: '/code/grade',
    method: 'POST',
    data,
    timeout: 600000,
  })
}
