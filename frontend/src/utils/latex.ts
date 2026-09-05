// LaTeX 相关纯函数与共享常量，供 LatexRenderer 与公式编辑器复用，
// 避免分隔符解析规则在两处漂移。

export interface LatexToken {
  content: string
  displayMode: boolean
}

export const latexCommandPattern =
  /\\(?:frac|dfrac|tfrac|sqrt|int|sum|prod|lim|log|ln|sin|cos|tan|cot|sec|csc|left|right|cdot|times|div|pm|mp|leq|geq|neq|approx|infty|alpha|beta|gamma|delta|theta|lambda|mu|pi|sigma|phi|omega|begin|end)\b/

export const latexScriptPattern = /(?:\^|_)(?:\{[^}]+\}|[A-Za-z0-9])/

// 识别整段内容中 `$...$` / `$$...$$` / `\(...\)` / `\[...\]` 包裹的公式。
export const latexTokenPattern =
  /(\$\$[\s\S]+?\$\$|\$[^$\n]+?\$|\\\[[\s\S]+?\\\]|\\\([\s\S]+?\\\))/g

export const looksLikeBareLatex = (text: string): boolean =>
  latexCommandPattern.test(text) || latexScriptPattern.test(text)

export const parseDelimitedLatex = (token: string): LatexToken => {
  if (token.startsWith('$$')) {
    return { content: token.slice(2, -2), displayMode: true }
  }

  if (token.startsWith('\\[')) {
    return { content: token.slice(2, -2), displayMode: true }
  }

  if (token.startsWith('\\(')) {
    return { content: token.slice(2, -2), displayMode: false }
  }

  return { content: token.slice(1, -1), displayMode: false }
}

export type LatexWrapMode = 'inline' | 'display' | 'none'

const placeholderPattern = /#\d+/g

// 将模板中的占位符（如 `#1`、`#2`）替换为可编辑的空花括号。
export const expandTemplate = (template: string): string =>
  template.replace(placeholderPattern, '{}')

// 清理源码中的残留占位符，保证输出是可直接渲染的 LaTeX。
export const stripPlaceholders = (source: string): string =>
  source.replace(placeholderPattern, '{}').trim()

// 去掉包裹分隔符，返回裸 LaTeX 源码。
export const unwrapLatex = (source: string): string => {
  const trimmed = source.trim()

  if (trimmed.startsWith('$$') && trimmed.endsWith('$$') && trimmed.length >= 4) {
    return trimmed.slice(2, -2)
  }

  if (trimmed.startsWith('\\[') && trimmed.endsWith('\\]')) {
    return trimmed.slice(2, -2)
  }

  if (trimmed.startsWith('\\(') && trimmed.endsWith('\\)')) {
    return trimmed.slice(2, -2)
  }

  if (trimmed.startsWith('$') && trimmed.endsWith('$') && trimmed.length >= 2) {
    return trimmed.slice(1, -1)
  }

  return source
}

// 按指定模式包裹 LaTeX：行内 `$...$`、独立 `$$...$$` 或无包裹。
export const wrapLatex = (source: string, mode: LatexWrapMode): string => {
  const bare = stripPlaceholders(unwrapLatex(source)).trim()
  if (!bare) {
    return ''
  }

  if (mode === 'inline') {
    return `$${bare}$`
  }

  if (mode === 'display') {
    return `$$${bare}$$`
  }

  return bare
}

// 在文本的 [start, end) 区间插入内容，返回新文本。
export const insertInto = (text: string, start: number, end: number, insertion: string): string =>
  text.slice(0, start) + insertion + text.slice(end)

// 从 Element Plus 输入框组件实例中取到原生 textarea 元素。
// 兼容 `textarea` / `ref` 暴露字段，以及 `$el.querySelector` 兜底。
export const resolveInputTextarea = (inputRef: unknown): HTMLTextAreaElement | null => {
  const inst = inputRef as
    | {
        textarea?: HTMLTextAreaElement
        ref?: HTMLElement
        $el?: HTMLElement
      }
    | null
    | undefined

  const textarea = inst?.textarea ?? (inst?.ref as HTMLTextAreaElement | undefined)
  if (textarea instanceof HTMLTextAreaElement) {
    return textarea
  }

  return inst?.$el?.querySelector('textarea') ?? null
}
