<template>
  <button
    type="button"
    class="latex-template-button"
    :title="`${name}：${latex}`"
    @click="emit('select')"
  >
    <span ref="renderRef" class="latex-template-button__render" />
    <span class="latex-template-button__label">{{ name }}</span>
  </button>
</template>

<script setup lang="ts">
import katex from 'katex'
import { onMounted, ref } from 'vue'

const props = defineProps<{
  name: string
  latex: string
}>()

const emit = defineEmits<{
  select: []
}>()

const renderRef = ref<HTMLElement>()

const render = () => {
  const el = renderRef.value
  if (!el) {
    return
  }

  el.replaceChildren()

  try {
    katex.render(props.latex, el, {
      displayMode: false,
      throwOnError: true,
    })
  } catch {
    el.textContent = props.name
  }
}

onMounted(render)
</script>

<style scoped>
.latex-template-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 68px;
  min-height: 68px;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s, box-shadow 0.1s;
}

.latex-template-button:hover {
  border-color: #409eff;
  background: #f5f9ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.18);
}

.latex-template-button:active {
  box-shadow: 0 1px 4px rgba(64, 158, 255, 0.28);
}

.latex-template-button__render {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 34px;
  color: #1f2937;
  font-size: 21px;
  line-height: 1.2;
}

.latex-template-button__render :deep(.katex) {
  font-size: 1em;
}

.latex-template-button__label {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.2;
}
</style>
