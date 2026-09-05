<template>
  <el-dialog
    :model-value="modelValue"
    title="插入公式"
    width="1000px"
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
            <LatexTemplateButton
              v-for="template in category.templates"
              :key="`${category.name}-${template.name}`"
              :name="template.name"
              :latex="template.latex"
              @select="insertTemplate(template.latex)"
            />
          </div>
        </el-tab-pane>
      </el-tabs>

      <div class="visual-editor">
        <div class="pane-title">可视化编辑</div>
        <div ref="mathfieldHost" class="mathfield-host" />
        <div class="editor-hint">点击公式区域即可用键盘直接输入数字与符号，也可输入 LaTeX 命令（如 \frac{1}{2}）。</div>
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
import { MathfieldElement } from 'mathlive'
import 'mathlive/fonts.css'
import { computed, nextTick, ref, watch } from 'vue'

import LatexTemplateButton from '@/components/LatexTemplateButton.vue'
import { latexTemplateCategories } from '@/data/latexTemplates'
import { wrapLatex, type LatexWrapMode } from '@/utils/latex'

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
const mathfieldHost = ref<HTMLElement>()
let mathfield: MathfieldElement | null = null

const outputSource = computed(() => wrapLatex(source.value, wrapMode.value))

const handleMathfieldInput = () => {
  if (mathfield) {
    source.value = mathfield.value
  }
}

const destroyMathfield = () => {
  if (mathfield) {
    mathfield.removeEventListener('input', handleMathfieldInput)
    mathfield.remove()
    mathfield = null
  }
}

const initMathfield = () => {
  destroyMathfield()

  const host = mathfieldHost.value
  if (!host) {
    return
  }

  const field = new MathfieldElement()
  field.value = source.value
  field.addEventListener('input', handleMathfieldInput)

  host.appendChild(field)
  mathfield = field
  field.focus()
}

const hideVirtualKeyboard = () => {
  if (window.mathVirtualKeyboard) {
    window.mathVirtualKeyboard.hide()
  }
}

const insertTemplate = (latex: string) => {
  if (mathfield) {
    mathfield.insert(latex)
    mathfield.focus()
    source.value = mathfield.value
    return
  }

  source.value += latex
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
      activeCategory.value = latexTemplateCategories[0]?.name ?? ''
      // 等待弹窗内容渲染完成后再初始化可视化编辑区（MathLive math-field）
      for (let i = 0; i < 10 && !mathfieldHost.value; i++) {
        await nextTick()
      }
      initMathfield()
    } else {
      hideVirtualKeyboard()
      destroyMathfield()
    }
  },
)
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
  max-height: 236px;
  overflow-y: auto;
  padding: 2px 2px 6px;
}

.visual-editor {
  display: grid;
  gap: 6px;
}

.pane-title {
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.mathfield-host {
  display: flex;
}

.mathfield-host :deep(math-field) {
  width: 100%;
  min-height: 120px;
  padding: 14px 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  box-sizing: border-box;
  font-size: 24px;
  line-height: 1.4;
}

.mathfield-host :deep(math-field:focus-within) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.12);
}

.editor-hint {
  font-size: 12px;
  color: #9ca3af;
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
