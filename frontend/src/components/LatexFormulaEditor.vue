<template>
  <el-dialog
    :model-value="modelValue"
    title="插入公式"
    width="980px"
    append-to-body
    destroy-on-close
    :close-on-press-escape="false"
    @close="handleClose"
  >
    <div class="formula-editor">
      <el-tabs v-model="activeCategory" class="template-tabs">
        <el-tab-pane
          v-for="category in latexTemplateCategories"
          :key="category.name"
          :label="category.name"
          :name="category.name"
        >
          <div class="template-grid">
            <button
              v-for="template in category.templates"
              :key="`${category.name}-${template.name}`"
              type="button"
              class="template-chip"
              :title="template.latex"
              @click="insertTemplate(template.latex)"
            >
              {{ template.name }}
            </button>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div class="editor-body">
        <section class="editor-pane">
          <div class="pane-title">源码编辑</div>
          <el-input
            ref="sourceInputRef"
            v-model="source"
            type="textarea"
            :rows="9"
            placeholder="在此输入 LaTeX 源码，或点击上方模板插入"
            class="source-input"
          />
        </section>

        <section class="editor-pane">
          <div class="pane-title">实时预览</div>
          <div class="preview-box">
            <el-empty v-if="!previewLatexText" description="暂无预览" :image-size="60" />
            <div v-else>
              <div ref="previewRef" class="preview-content" />
              <div v-if="previewError" class="preview-error">{{ previewError }}</div>
            </div>
          </div>
        </section>
      </div>

      <div class="editor-output">
        <div class="output-header">
          <div class="pane-title">输出源码</div>
          <el-radio-group v-model="wrapMode" size="small">
            <el-radio-button value="inline">行内 $…$</el-radio-button>
            <el-radio-button value="display">独立 $$…$$</el-radio-button>
            <el-radio-button value="none">无包裹</el-radio-button>
          </el-radio-group>
        </div>
        <div class="output-row">
          <el-input
            :model-value="outputSource"
            type="textarea"
            :rows="2"
            readonly
            class="output-input"
          />
          <el-button class="copy-button" @click="copySource">复制源码</el-button>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm">插入</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import katex from 'katex'
import { computed, nextTick, ref, watch } from 'vue'

import { latexTemplateCategories } from '@/data/latexTemplates'
import {
  expandTemplate,
  resolveInputTextarea,
  stripPlaceholders,
  unwrapLatex,
  wrapLatex,
  type LatexWrapMode,
} from '@/utils/latex'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: [source: string]
}>()

const source = ref('')
const wrapMode = ref<LatexWrapMode>('inline')
const activeCategory = ref(latexTemplateCategories[0]?.name ?? '')
const sourceInputRef = ref<any>()
const previewRef = ref<HTMLElement>()
const previewError = ref('')

const previewLatexText = computed(() => stripPlaceholders(unwrapLatex(source.value)))

const outputSource = computed(() => wrapLatex(source.value, wrapMode.value))

const renderPreview = () => {
  const latex = previewLatexText.value
  const el = previewRef.value

  if (!latex) {
    previewError.value = ''
    el?.replaceChildren()
    return
  }

  if (!el) {
    return
  }

  el.replaceChildren()

  try {
    katex.render(latex, el, {
      displayMode: wrapMode.value === 'display',
      throwOnError: true,
    })
    previewError.value = ''
  } catch (error) {
    previewError.value = error instanceof Error ? error.message : '公式存在语法错误'
  }
}

const insertTemplate = (template: string) => {
  const latex = expandTemplate(template)
  const textarea = resolveInputTextarea(sourceInputRef.value)
  const start = textarea?.selectionStart ?? source.value.length
  const end = textarea?.selectionEnd ?? start

  source.value = source.value.slice(0, start) + latex + source.value.slice(end)

  nextTick(() => {
    const el = resolveInputTextarea(sourceInputRef.value)
    if (el) {
      const pos = start + latex.length
      el.setSelectionRange(pos, pos)
      el.focus()
    }
  })
}

const copySource = async () => {
  const text = outputSource.value
  if (!text) {
    ElMessage.warning('暂无可复制的公式源码')
    return
  }

  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('公式源码已复制')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
}

const handleConfirm = () => {
  const text = outputSource.value
  if (!text) {
    ElMessage.warning('请先输入或选择公式')
    return
  }

  emit('confirm', text)
  emit('update:modelValue', false)
}

watch(
  () => props.modelValue,
  async (visible) => {
    if (visible) {
      source.value = ''
      wrapMode.value = 'inline'
      previewError.value = ''
      activeCategory.value = latexTemplateCategories[0]?.name ?? ''
      await nextTick()
      renderPreview()
    }
  },
)

watch([source, wrapMode], () => {
  nextTick(renderPreview)
})
</script>

<style scoped>
.formula-editor {
  display: grid;
  gap: 14px;
}

.template-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 2px 0 4px;
}

.template-chip {
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
  color: #374151;
  cursor: pointer;
  font-size: 13px;
  transition: border-color 0.15s, color 0.15s, background 0.15s;
}

.template-chip:hover {
  border-color: #409eff;
  color: #409eff;
  background: #f5f9ff;
}

.editor-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.editor-pane {
  min-width: 0;
}

.pane-title {
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.preview-box {
  min-height: 220px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fbfbfc;
  overflow: auto;
}

.preview-content {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  padding: 8px;
  overflow-x: auto;
}

.preview-error {
  margin-top: 8px;
  color: #c45656;
  font-size: 13px;
  word-break: break-word;
}

.editor-output {
  display: grid;
  gap: 6px;
}

.output-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.output-header .pane-title {
  margin-bottom: 0;
}

.output-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.output-input {
  flex: 1;
}

.copy-button {
  flex-shrink: 0;
  margin-top: 2px;
}
</style>
