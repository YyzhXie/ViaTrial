<template>
  <div class="markdown-editor">
    <div class="markdown-editor-toolbar">
      <div class="markdown-tabs">
        <button type="button" :class="{ active: mode === 'write' }" @click="mode = 'write'">编写</button>
        <button type="button" :class="{ active: mode === 'preview' }" @click="mode = 'preview'">预览</button>
      </div>
      <el-button v-if="mode === 'write'" size="small" @click="$emit('formula')">插入公式</el-button>
    </div>
    <el-input v-if="mode === 'write'" ref="inputRef" :model-value="modelValue" type="textarea" :rows="rows" :placeholder="placeholder" @update:model-value="$emit('update:modelValue', $event)" />
    <div v-else class="markdown-preview"><MarkdownRenderer :content="modelValue" /></div>
    <div class="markdown-editor-footer">支持 Markdown 格式 · 公式可使用 LaTeX</div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import MarkdownRenderer from './MarkdownRenderer.vue'

withDefaults(defineProps<{ modelValue: string; placeholder?: string; rows?: number }>(), { placeholder: '', rows: 4 })
defineEmits<{ 'update:modelValue': [value: string]; formula: [] }>()
const mode = ref<'write' | 'preview'>('write')
const inputRef = ref()
defineExpose({ inputRef })
</script>

<style scoped>
.markdown-editor { width: 100%; overflow: hidden; border: 1px solid #d1d9e0; border-radius: 6px; background: white; }
.markdown-editor-toolbar { display: flex; align-items: center; justify-content: space-between; min-height: 44px; padding: 0 12px; border-bottom: 1px solid #d1d9e0; background: #f6f8fa; }
.markdown-tabs { display: flex; gap: 16px; height: 44px; }
.markdown-tabs button { padding: 0 4px; border: 0; border-bottom: 2px solid transparent; background: transparent; color: #59636e; cursor: pointer; }
.markdown-tabs button.active { border-bottom-color: #fd8c73; color: #1f2328; font-weight: 600; }
.markdown-editor :deep(.el-textarea__inner) { min-height: 100px; border: 0; border-radius: 0; box-shadow: none; resize: vertical; }
.markdown-preview { min-height: 100px; padding: 12px; }
.markdown-editor-footer { padding: 7px 12px; border-top: 1px solid #d1d9e0; background: #f6f8fa; color: #59636e; font-size: 12px; }
</style>
