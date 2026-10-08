<template>
  <main class="page-shell question-form-page">
    <header class="question-form-header">
      <div><h1>{{ dialogTitle }}</h1><p>使用 Markdown 编写题目内容，并可随时预览效果。</p></div>
      <div class="question-form-actions"><el-button @click="handleClose">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">保存题目</el-button></div>
    </header>
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="96px"
      class="question-form"
    >
      <el-form-item label="科目" prop="subjectId">
        <el-select
          v-model="form.subjectId"
          allow-create
          default-first-option
          filterable
          placeholder="选择科目"
          @change="handleSubjectChange"
        >
          <el-option
            v-for="subject in subjects"
            :key="subject.id"
            :label="subject.name"
            :value="subject.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="题型" prop="typeId">
        <el-select
          v-model="form.typeId"
          :disabled="!form.subjectId"
          :loading="typeLoading"
          allow-create
          default-first-option
          filterable
          placeholder="选择题型"
        >
          <el-option
            v-for="type in questionTypes"
            :key="type.id"
            :label="type.name"
            :value="type.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="题目" prop="content">
        <MarkdownEditor ref="contentInputRef" v-model="form.content" :rows="4" placeholder="输入题目正文，支持 Markdown 和 LaTeX" @formula="openFormulaEditor('content')" />
      </el-form-item>

      <el-form-item label="答案">
        <MarkdownEditor ref="answerInputRef" v-model="form.answer" :rows="3" placeholder="输入答案，支持 Markdown 和 LaTeX" @formula="openFormulaEditor('answer')" />
      </el-form-item>

      <el-form-item label="解析">
        <MarkdownEditor ref="analysisInputRef" v-model="form.analysis" :rows="3" placeholder="输入解析，支持 Markdown 和 LaTeX" @formula="openFormulaEditor('analysis')" />
      </el-form-item>

      <el-form-item label="题目图片">
        <el-input v-model="form.imageUrl" placeholder="输入图片 URL" />
      </el-form-item>

      <el-form-item label="答案图片">
        <el-input v-model="form.answerImageUrl" placeholder="输入答案图片 URL" />
      </el-form-item>

      <el-form-item label="难度" prop="difficulty">
        <el-radio-group v-model="form.difficulty">
          <el-radio-button :value="1">1</el-radio-button>
          <el-radio-button :value="2">2</el-radio-button>
          <el-radio-button :value="3">3</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="标签">
        <TagSelector v-model="selectedTagIds" />
      </el-form-item>
    </el-form>

    <LatexFormulaEditor v-model="formulaDialogVisible" @confirm="handleFormulaConfirm" />

  </main>
</template>

<script setup lang="ts">
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { addQuestion, getQuestion, updateQuestion } from '@/api/question'
import { addQuestionType, listQuestionTypes } from '@/api/questionType'
import { addSubject, listSubjects } from '@/api/subject'
import { addTag, listTags } from '@/api/tag'
import LatexFormulaEditor from '@/components/LatexFormulaEditor.vue'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import TagSelector from '@/components/TagSelector.vue'
import { insertInto, resolveInputTextarea } from '@/utils/latex'
import type { Question, QuestionAddRequest } from '@/types/question'
import type { QuestionType } from '@/types/questionType'
import type { Subject } from '@/types/subject'
import type { Tag } from '@/types/tag'

const route = useRoute()
const router = useRouter()
const editingQuestion = ref<Question | null>(null)

type QuestionFormState = Omit<QuestionAddRequest, 'subjectId' | 'typeId' | 'tagIds' | 'answer' | 'analysis'> & {
  subjectId: number | string
  typeId: number | string
  answer: string
  analysis: string
  tagIds: Array<number | string>
}

const createInitialForm = (): QuestionFormState => ({
  subjectId: undefined as unknown as number | string,
  typeId: undefined as unknown as number | string,
  content: '',
  answer: '',
  analysis: '',
  imageUrl: '',
  answerImageUrl: '',
  difficulty: 1,
  tagIds: [],
})

const formRef = ref<FormInstance>()
const form = reactive<QuestionFormState>(createInitialForm())
const subjects = ref<Subject[]>([])
const questionTypes = ref<QuestionType[]>([])
const typeLoading = ref(false)
const submitting = ref(false)
const dialogTitle = computed(() => (editingQuestion.value ? '编辑题目' : '新增题目'))

type FormulaTarget = 'content' | 'answer' | 'analysis'

const contentInputRef = ref<any>()
const answerInputRef = ref<any>()
const analysisInputRef = ref<any>()
const formulaDialogVisible = ref(false)
const formulaTarget = ref<FormulaTarget | null>(null)

const getInputRef = (target: FormulaTarget) => {
  if (target === 'content') {
    return contentInputRef
  }
  if (target === 'answer') {
    return answerInputRef
  }
  return analysisInputRef
}

const openFormulaEditor = (target: FormulaTarget) => {
  formulaTarget.value = target
  formulaDialogVisible.value = true
}

const handleFormulaConfirm = (source: string) => {
  const target = formulaTarget.value
  if (!target) {
    return
  }

  const textarea = resolveInputTextarea(getInputRef(target).value?.inputRef)
  const current = form[target] ?? ''
  const start = textarea?.selectionStart ?? current.length
  const end = textarea?.selectionEnd ?? start

  form[target] = insertInto(current, start, end, source)

  nextTick(() => {
    const el = resolveInputTextarea(getInputRef(target).value?.inputRef)
    if (el) {
      const pos = start + source.length
      el.setSelectionRange(pos, pos)
      el.focus()
    }
  })
}

const selectedTagIds = computed<Array<number | string>>({
  get: () => form.tagIds || [],
  set: (value) => {
    form.tagIds = value
  },
})

const rules: FormRules<QuestionFormState> = {
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  typeId: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
}

const resetForm = () => {
  Object.assign(form, createInitialForm())
  questionTypes.value = []
  formRef.value?.clearValidate()
}

const fillForm = async (question: Question) => {
  Object.assign(form, {
    subjectId: question.subjectId,
    typeId: question.typeId,
    content: question.content,
    answer: question.answer ?? '',
    analysis: question.analysis ?? '',
    imageUrl: question.imageUrl ?? '',
    answerImageUrl: question.answerImageUrl ?? '',
    difficulty: question.difficulty,
    tagIds: question.tags.map((tag) => tag.id),
  })
  await loadQuestionTypes(question.subjectId)
  formRef.value?.clearValidate()
}

const normalizeText = (value?: string | null) => {
  const text = value?.trim()
  return text ? text : null
}

const loadSubjects = async () => {
  subjects.value = await listSubjects()
}

const loadQuestionTypes = async (subjectId: number) => {
  typeLoading.value = true
  try {
    questionTypes.value = await listQuestionTypes(subjectId)
  } finally {
    typeLoading.value = false
  }
}

const handleSubjectChange = async (subjectId: number | string) => {
  form.typeId = undefined as unknown as number | string
  questionTypes.value = []

  if (typeof subjectId === 'number') {
    await loadQuestionTypes(subjectId)
  }
}

const normalizeCreatedName = (value: string, fieldName: string) => {
  const name = value.trim()
  if (!name) {
    throw new Error(`${fieldName}不能为空`)
  }
  return name
}

const ensureSubjectId = async () => {
  if (typeof form.subjectId === 'number') {
    return form.subjectId
  }

  const name = normalizeCreatedName(String(form.subjectId ?? ''), '科目名称')
  const existingSubject = subjects.value.find((subject) => subject.name === name)
  if (existingSubject) {
    return existingSubject.id
  }

  return addSubject({ name })
}

const ensureTypeId = async (subjectId: number) => {
  if (typeof form.typeId === 'number') {
    return form.typeId
  }

  const name = normalizeCreatedName(String(form.typeId ?? ''), '题型名称')
  const typeList = questionTypes.value.length ? questionTypes.value : await listQuestionTypes(subjectId)
  const existingType = typeList.find((type) => type.name === name)
  if (existingType) {
    return existingType.id
  }

  return addQuestionType({ subjectId, name })
}

const ensureTagIds = async () => {
  const values = selectedTagIds.value
  const tagIds: number[] = []
  const tagNames = new Set<string>()
  const typedTagNames = values.filter((value) => typeof value === 'string')
  const existingTags: Tag[] = typedTagNames.length ? await listTags() : []

  for (const value of values) {
    if (typeof value === 'number') {
      tagIds.push(value)
      continue
    }

    const name = normalizeCreatedName(String(value), '标签名称')
    if (tagNames.has(name)) {
      continue
    }
    tagNames.add(name)

    const existingTag = existingTags.find((tag) => tag.name === name)
    if (existingTag) {
      tagIds.push(existingTag.id)
    } else {
      tagIds.push(await addTag({ name }))
    }
  }

  return tagIds
}

const handleClose = () => {
  router.push('/')
}

const handleSubmit = async () => {
  await formRef.value?.validate()

  submitting.value = true
  try {
    const subjectId = await ensureSubjectId()
    const typeId = await ensureTypeId(subjectId)
    const tagIds = await ensureTagIds()

    const payload = {
      subjectId,
      typeId,
      content: form.content.trim(),
      answer: normalizeText(form.answer),
      analysis: normalizeText(form.analysis),
      imageUrl: normalizeText(form.imageUrl),
      answerImageUrl: normalizeText(form.answerImageUrl),
      difficulty: form.difficulty,
      tagIds,
    }

    if (editingQuestion.value) {
      await updateQuestion(editingQuestion.value.id, payload)
      ElMessage.success('编辑题目成功')
    } else {
      await addQuestion(payload)
      ElMessage.success('新增题目成功')
    }
    await router.push('/')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  resetForm()
  await loadSubjects()
  const id = Number(route.params.id)
  if (Number.isInteger(id) && id > 0) {
    try {
      const question = await getQuestion(id)
      editingQuestion.value = question
      await fillForm(question)
    } catch {
      ElMessage.error('未找到该题目')
      await router.replace('/')
    }
  }
})
</script>

<style scoped>
.question-form-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 20px; }
.question-form-header h1 { margin: 0; color: #1f2328; font-size: 24px; }
.question-form-header p { margin: 6px 0 0; color: #59636e; }
.question-form-actions { display: flex; gap: 8px; }
.question-form { padding: 24px; border: 1px solid #d1d9e0; border-radius: 8px; background: #fff; }
.question-form :deep(.el-select) {
  width: 100%;
}

</style>
