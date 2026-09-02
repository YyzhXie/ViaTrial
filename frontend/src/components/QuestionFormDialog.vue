<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="720px"
    destroy-on-close
    :close-on-press-escape="false"
    @close="handleClose"
  >
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
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="输入题目正文，可包含 LaTeX"
        />
      </el-form-item>

      <el-form-item label="答案">
        <el-input v-model="form.answer" type="textarea" :rows="3" placeholder="输入答案" />
      </el-form-item>

      <el-form-item label="解析">
        <el-input v-model="form.analysis" type="textarea" :rows="3" placeholder="输入解析" />
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

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { computed, reactive, ref, watch } from 'vue'

import { addQuestion, updateQuestion } from '@/api/question'
import { addQuestionType, listQuestionTypes } from '@/api/questionType'
import { addSubject, listSubjects } from '@/api/subject'
import { addTag, listTags } from '@/api/tag'
import TagSelector from '@/components/TagSelector.vue'
import type { Question, QuestionAddRequest } from '@/types/question'
import type { QuestionType } from '@/types/questionType'
import type { Subject } from '@/types/subject'
import type { Tag } from '@/types/tag'

const props = defineProps<{
  modelValue: boolean
  question?: Question | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

type QuestionFormState = Omit<QuestionAddRequest, 'subjectId' | 'typeId' | 'tagIds'> & {
  subjectId: number | string
  typeId: number | string
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
const dialogTitle = computed(() => (props.question ? '编辑题目' : '新增题目'))

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
  emit('update:modelValue', false)
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

    if (props.question) {
      await updateQuestion(props.question.id, payload)
      ElMessage.success('编辑题目成功')
    } else {
      await addQuestion(payload)
      ElMessage.success('新增题目成功')
    }
    emit('update:modelValue', false)
    emit('success')
  } finally {
    submitting.value = false
  }
}

watch(
  () => props.modelValue,
  async (visible) => {
    if (visible) {
      resetForm()
      await loadSubjects()
      if (props.question) {
        await fillForm(props.question)
      }
    }
  },
)
</script>

<style scoped>
.question-form :deep(.el-select) {
  width: 100%;
}
</style>
