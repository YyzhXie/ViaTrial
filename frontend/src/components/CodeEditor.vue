<template>
  <div ref="host" class="code-editor-host" />
</template>

<script setup lang="ts">
import { basicSetup } from 'codemirror'
import { cpp } from '@codemirror/lang-cpp'
import { java } from '@codemirror/lang-java'
import { python } from '@codemirror/lang-python'
import { indentWithTab } from '@codemirror/commands'
import { indentUnit } from '@codemirror/language'
import { Compartment, EditorState } from '@codemirror/state'
import { EditorView, keymap } from '@codemirror/view'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

import type { ProgrammingLanguage } from '@/utils/programmingQuestion'

const props = withDefaults(defineProps<{
  modelValue: string
  language: ProgrammingLanguage
  minHeight?: string
  maxHeight?: string
  readonly?: boolean
}>(), { minHeight: '320px', maxHeight: '70vh', readonly: false })

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
const host = ref<HTMLElement>()
let view: EditorView | undefined
const languageSlot = new Compartment()
const editableSlot = new Compartment()

const languageExtension = (language: ProgrammingLanguage) => {
  if (language === 'java') return java()
  if (language === 'python') return python()
  return cpp()
}

onMounted(() => {
  if (!host.value) return
  view = new EditorView({
    state: EditorState.create({
      doc: props.modelValue,
      extensions: [
        basicSetup,
        EditorState.tabSize.of(4),
        indentUnit.of('    '),
        keymap.of([indentWithTab]),
        languageSlot.of(languageExtension(props.language)),
        editableSlot.of(EditorView.editable.of(!props.readonly)),
        EditorView.updateListener.of((update) => {
          if (update.docChanged) emit('update:modelValue', update.state.doc.toString())
        }),
        EditorView.theme({
          '&': { minHeight: props.minHeight, maxHeight: props.maxHeight, fontSize: '14px' },
          '.cm-scroller': { overflow: 'auto', fontFamily: 'Consolas, "Cascadia Code", monospace' },
          '.cm-content': { padding: '14px 0' },
          '.cm-gutters': { backgroundColor: '#f6f8fa', borderRight: '1px solid #d1d9e0' },
          '&.cm-focused': { outline: '2px solid #409eff', outlineOffset: '-2px' },
        }),
      ],
    }),
    parent: host.value,
  })
})

watch(() => props.modelValue, (value) => {
  if (!view || view.state.doc.toString() === value) return
  view.dispatch({ changes: { from: 0, to: view.state.doc.length, insert: value } })
})

watch(() => props.language, (value) => {
  view?.dispatch({ effects: languageSlot.reconfigure(languageExtension(value)) })
})

watch(() => props.readonly, (value) => {
  view?.dispatch({ effects: editableSlot.reconfigure(EditorView.editable.of(!value)) })
})

onBeforeUnmount(() => {
  view?.destroy()
  view = undefined
})
</script>

<style scoped>
.code-editor-host { width: 100%; overflow: hidden; border: 1px solid #d1d9e0; border-radius: 6px; background: #fff; }
.code-editor-host :deep(.cm-editor) { width: 100%; }
</style>
