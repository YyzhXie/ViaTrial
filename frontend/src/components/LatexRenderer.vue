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

const appendText = (text: string) => {
  containerRef.value?.appendChild(document.createTextNode(text))
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

  const tokenPattern = /(\$\$[\s\S]+?\$\$|\$[^$\n]+?\$)/g
  let cursor = 0

  for (const match of content.matchAll(tokenPattern)) {
    const token = match[0]
    const index = match.index ?? 0

    if (index > cursor) {
      appendText(content.slice(cursor, index))
    }

    const displayMode = token.startsWith('$$')
    const latex = displayMode ? token.slice(2, -2) : token.slice(1, -1)
    const span = document.createElement(displayMode ? 'div' : 'span')

    try {
      katex.render(latex, span, {
        displayMode,
        throwOnError: false,
      })
      containerRef.value.appendChild(span)
    } catch {
      appendText(token)
    }

    cursor = index + token.length
  }

  if (cursor < content.length) {
    appendText(content.slice(cursor))
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
</style>
