<template>
  <span ref="containerRef" class="latex-renderer" />
</template>

<script setup lang="ts">
import katex from 'katex'
import { onMounted, ref, watch } from 'vue'

const props = defineProps<{
  content?: string | null
}>()

const containerRef = ref<HTMLElement>()

type LatexToken = {
  content: string
  displayMode: boolean
}

const latexCommandPattern =
  /\\(?:frac|dfrac|tfrac|sqrt|int|sum|prod|lim|log|ln|sin|cos|tan|cot|sec|csc|left|right|cdot|times|div|pm|mp|leq|geq|neq|approx|infty|alpha|beta|gamma|delta|theta|lambda|mu|pi|sigma|phi|omega|begin|end)\b/

const latexScriptPattern = /(?:\^|_)(?:\{[^}]+\}|[A-Za-z0-9])/

const looksLikeBareLatex = (text: string) =>
  latexCommandPattern.test(text) || latexScriptPattern.test(text)

const appendText = (text: string) => {
  containerRef.value?.appendChild(document.createTextNode(text))
}

const appendLatex = (token: LatexToken, fallback: string) => {
  if (!containerRef.value) {
    return
  }

  const node = document.createElement(token.displayMode ? 'div' : 'span')

  try {
    katex.render(token.content, node, {
      displayMode: token.displayMode,
      throwOnError: false,
    })
    containerRef.value.appendChild(node)
  } catch {
    appendText(fallback)
  }
}

const parseDelimitedLatex = (token: string): LatexToken => {
  if (token.startsWith('$$')) {
    return {
      content: token.slice(2, -2),
      displayMode: true,
    }
  }

  if (token.startsWith('\\[')) {
    return {
      content: token.slice(2, -2),
      displayMode: true,
    }
  }

  if (token.startsWith('\\(')) {
    return {
      content: token.slice(2, -2),
      displayMode: false,
    }
  }

  return {
    content: token.slice(1, -1),
    displayMode: false,
  }
}

const appendTextOrBareLatex = (text: string) => {
  if (!text) {
    return
  }

  const trimmed = text.trim()
  const leadingWhitespace = text.slice(0, text.indexOf(trimmed))
  const trailingWhitespace = text.slice(text.indexOf(trimmed) + trimmed.length)

  if (trimmed && looksLikeBareLatex(trimmed)) {
    appendText(leadingWhitespace)
    appendLatex(
      {
        content: trimmed,
        displayMode: false,
      },
      trimmed,
    )
    appendText(trailingWhitespace)
    return
  }

  appendText(text)
}

const renderLatex = () => {
  if (!containerRef.value) {
    return
  }

  const content = props.content || ''
  containerRef.value.replaceChildren()

  if (!content) {
    return
  }

  const tokenPattern = /(\$\$[\s\S]+?\$\$|\$[^$\n]+?\$|\\\[[\s\S]+?\\\]|\\\([\s\S]+?\\\))/g
  let cursor = 0

  for (const match of content.matchAll(tokenPattern)) {
    const token = match[0]
    const index = match.index ?? 0

    if (index > cursor) {
      appendTextOrBareLatex(content.slice(cursor, index))
    }

    appendLatex(parseDelimitedLatex(token), token)
    cursor = index + token.length
  }

  if (cursor < content.length) {
    appendTextOrBareLatex(content.slice(cursor))
  }
}

onMounted(renderLatex)
watch(() => props.content, renderLatex)
</script>

<style scoped>
.latex-renderer {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  line-height: 1.7;
}

.latex-renderer :deep(.katex) {
  font-size: 1.05em;
}

.latex-renderer :deep(.katex-display) {
  margin: 0.4em 0;
  overflow-x: auto;
  overflow-y: hidden;
}
</style>
