declare module 'markdown-it-katex' {
  import type MarkdownIt from 'markdown-it'

  const katexPlugin: (md: MarkdownIt, options?: Record<string, unknown>) => void
  export default katexPlugin
}
