<template>
  <el-dialog
    :model-value="modelValue"
    title="新增题目"
    width="720px"
    destroy-on-close
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

import { addQuestion } from '@/api/question'
import { listQuestionTypes } from '@/api/questionType'
import { listSubjects } from '@/api/subject'
import TagSelector from '@/components/TagSelector.vue'
import type { QuestionAddRequest } from '@/types/question'
import type { QuestionType } from '@/types/questionType'
import type { Subject } from '@/types/subject'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const createInitialForm = (): QuestionAddRequest => ({
  subjectId: undefined as unknown as number,
  typeId: undefined as unknown as number,
  content: '',
  answer: '',
  analysis: '',
  imageUrl: '',
  answerImageUrl: '',
  difficulty: 1,
  tagIds: [],
})

const formRef = ref<FormInstance>()
const form = reactive<QuestionAddRequest>(createInitialForm())
const subjects = ref<Subject[]>([])
const questionTypes = ref<QuestionType[]>([])
const typeLoading = ref(false)
const submitting = ref(false)

const selectedTagIds = computed<number[]>({
  get: () => form.tagIds || [],
  set: (value) => {
    form.tagIds = value
  },
})

const rules: FormRules<QuestionAddRequest> = {
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

const handleSubjectChange = async (subjectId: number) => {
  form.typeId = undefined as unknown as number
  questionTypes.value = []

  if (subjectId) {
    await loadQuestionTypes(subjectId)
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
}

const handleSubmit = async () => {
  await formRef.value?.validate()

  submitting.value = true
  try {
    await addQuestion({
      subjectId: form.subjectId,
      typeId: form.typeId,
      content: form.content.trim(),
      answer: normalizeText(form.answer),
      analysis: normalizeText(form.analysis),
      imageUrl: normalizeText(form.imageUrl),
      answerImageUrl: normalizeText(form.answerImageUrl),
      difficulty: form.difficulty,
      tagIds: form.tagIds,
    })
    ElMessage.success('新增题目成功')
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
    }
  },
)
</script>

<style scoped>
.question-form :deep(.el-select) {
  width: 100%;
}
</style>
