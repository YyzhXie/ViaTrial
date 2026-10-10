<template>
  <main class="page-shell question-form-page">
    <header class="question-form-header">
      <div><h1>{{ dialogTitle }}</h1></div>
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

      <el-form-item label="题型">
        <el-checkbox :model-value="questionKind === 'choice'" :disabled="!form.subjectId" @change="setQuestionKind('choice')">选择题</el-checkbox>
        <el-checkbox :model-value="questionKind === 'fill'" :disabled="!form.subjectId" @change="setQuestionKind('fill')">填空题</el-checkbox>
        <el-checkbox :model-value="questionKind === 'programming'" :disabled="!form.subjectId" @change="setQuestionKind('programming')">编程题</el-checkbox>
      </el-form-item>

      <el-form-item v-if="questionKind !== 'programming'" label="题目" prop="content">
        <MarkdownEditor ref="contentInputRef" v-model="form.content" :rows="4" placeholder="输入题目正文，支持 Markdown 和 LaTeX" @formula="openFormulaEditor('content')" />
      </el-form-item>

      <template v-if="questionKind === 'programming'">
        <el-form-item label="题目描述" prop="content">
          <MarkdownEditor ref="contentInputRef" v-model="form.content" :rows="5" placeholder="描述问题、输入格式、输出格式和约束" @formula="openFormulaEditor('content')" />
        </el-form-item>
        <el-form-item label="编程语言">
          <el-select v-model="programmingLanguage">
            <el-option label="C" value="c" />
            <el-option label="C++" value="cpp" />
            <el-option label="Java" value="java" />
            <el-option label="Python 3" value="python" />
          </el-select>
        </el-form-item>
        <el-form-item label="竞赛模式">
          <el-switch v-model="competitionMode" active-text="开启" inactive-text="关闭" />
        </el-form-item>
        <template v-if="competitionMode">
          <el-form-item label="时间限制">
            <div class="program-limit-input"><el-input-number v-model="timeLimitMs" :min="50" :max="30000" :step="100" controls-position="right" /><span>毫秒</span></div>
          </el-form-item>
          <el-form-item label="内存限制">
            <div class="program-limit-input"><el-input-number v-model="memoryLimitMb" :min="16" :max="2048" :step="16" controls-position="right" /><span>MB</span></div>
          </el-form-item>
        </template>
        <el-form-item label="参考代码">
          <CodeEditor v-model="starterCode" :language="programmingLanguage" min-height="300px" />
        </el-form-item>
        <el-form-item label="自定义测试点">
          <div class="program-test-list">
            <div v-for="(testCase, index) in programmingTestCases" :key="index" class="program-test-case">
              <div class="program-test-heading"><strong>测试点 {{ index + 1 }}</strong><div class="program-test-actions"><el-checkbox v-model="testCase.isSample">测试样例</el-checkbox><el-button v-if="programmingTestCases.length > 1" text type="danger" @click="programmingTestCases.splice(index, 1)">删除</el-button></div></div>
              <div class="program-test-fields">
                <el-input v-model="testCase.input" type="textarea" :rows="3" placeholder="标准输入（stdin）" />
                <el-input v-model="testCase.expectedOutput" type="textarea" :rows="3" placeholder="期望输出" />
              </div>
            </div>
            <el-button plain :disabled="programmingTestCases.length >= 20" @click="programmingTestCases.push({ input: '', expectedOutput: '', isSample: false })">添加测试点</el-button>
          </div>
        </el-form-item>
        <el-form-item label="编译测试">
          <div class="program-run-panel">
            <el-button type="success" :loading="runningCode" :disabled="!starterCode.trim() || !programmingTestCases.length" @click="runProgrammingTestCases">编译并运行所有测试点</el-button>
            <div v-for="(result, index) in programmingRunResults" :key="index" class="program-run-result">
              <el-tag :type="result.verdict === 'AC' ? 'success' : 'danger'">测试点 {{ index + 1 }} · {{ result.verdict }}</el-tag>
              <span>{{ result.status }}<template v-if="result.time"> · {{ result.time }}s</template><template v-if="result.memory"> · {{ result.memory }}</template></span>
              <pre v-if="result.compileOutput || result.stderr || result.stdout">{{ result.compileOutput || result.stderr || result.stdout }}</pre>
            </div>
          </div>
        </el-form-item>
      </template>

      <template v-if="questionKind === 'choice'">
        <el-form-item label="选项">
          <div class="choice-option-list">
            <div v-for="(option, index) in options" :key="index" class="choice-option-row">
              <button type="button" class="option-letter" :class="{ selected: correctOptions.includes(index), multiple: multipleChoice }" @click="toggleCorrect(index)">{{ String.fromCharCode(65 + index) }}</button>
              <el-input v-model="option.text" :placeholder="`选项 ${String.fromCharCode(65 + index)}`" />
              <el-button text type="danger" @click="removeOption(index)">删除</el-button>
            </div>
            <div v-if="options.length < 26" class="choice-option-row add-option-row">
              <button type="button" class="option-letter add-option" :class="{ multiple: multipleChoice }" aria-label="添加选项" @click="options.push({ text: '' })">+</button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="多选模式">
          <el-switch v-model="multipleChoice" active-text="开启" inactive-text="关闭" @change="handleMultipleModeChange" />
        </el-form-item>
      </template>

      <template v-else-if="questionKind === 'fill'">
        <el-form-item label="答案匹配">
          <el-radio-group v-model="fillAnswerMode">
            <el-radio-button value="alternatives">多种正确写法</el-radio-button>
            <el-radio-button value="blanks">多个空</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-for="(_, index) in fillAnswers" :key="index" :label="index === 0 ? '答案' : `答案${index + 1}`">
          <div class="fill-answer-row">
            <MarkdownEditor :ref="(el: any) => setFillAnswerRef(index, el)" v-model="fillAnswers[index]" :rows="2" placeholder="输入答案，支持 Markdown 和 LaTeX" />
            <el-button text type="danger" @click="removeFillAnswer(index)">删除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="">
          <el-button plain @click="fillAnswers.push('')">添加答案</el-button>
        </el-form-item>
      </template>

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
import { executeCode, type CodeExecutionResult } from '@/api/codeExecution'
import CodeEditor from '@/components/CodeEditor.vue'
import LatexFormulaEditor from '@/components/LatexFormulaEditor.vue'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import TagSelector from '@/components/TagSelector.vue'
import { insertInto, resolveInputTextarea } from '@/utils/latex'
import { decodeAnswerData, decodeQuestionContent, encodeAnswers, encodeQuestionContent, isChoiceType, type FillAnswerMode } from '@/utils/questionFormat'
import type { Question, QuestionAddRequest } from '@/types/question'
import type { QuestionType } from '@/types/questionType'
import type { Subject } from '@/types/subject'
import type { Tag } from '@/types/tag'
import { decodeProgrammingQuestion, encodeProgrammingQuestion, MAX_PROGRAMMING_QUESTION_LENGTH, type ProgrammingLanguage, type ProgrammingTestCase } from '@/utils/programmingQuestion'

const route = useRoute()
const router = useRouter()
const editingQuestion = ref<Question | null>(null)
const questionKind = ref<'choice' | 'fill' | 'programming'>('choice')
const options = ref<Array<{ text: string }>>([{ text: '' }, { text: '' }])
const correctOptions = ref<number[]>([])
const multipleChoice = ref(false)
const fillAnswers = ref<string[]>([''])
const fillAnswerMode = ref<FillAnswerMode>('alternatives')
const fillAnswerRefs = ref<any[]>([])
const programmingLanguage = ref<ProgrammingLanguage>('cpp')
const competitionMode = ref(false)
const timeLimitMs = ref(1000)
const memoryLimitMb = ref(256)
const starterCode = ref('')
const programmingTestCases = ref<ProgrammingTestCase[]>([{ input: '', expectedOutput: '', isSample: false }])
const programmingRunResults = ref<CodeExecutionResult[]>([])
const runningCode = ref(false)

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
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
}

const resetForm = () => {
  Object.assign(form, createInitialForm())
  questionTypes.value = []
  questionKind.value = 'choice'
  programmingLanguage.value = 'cpp'
  competitionMode.value = false
  timeLimitMs.value = 1000
  memoryLimitMb.value = 256
  starterCode.value = ''
  programmingTestCases.value = [{ input: '', expectedOutput: '', isSample: false }]
  programmingRunResults.value = []
  options.value = [{ text: '' }, { text: '' }]
  correctOptions.value = []
  multipleChoice.value = false
  fillAnswers.value = ['']
  fillAnswerMode.value = 'alternatives'
  fillAnswerRefs.value = []
  formRef.value?.clearValidate()
}

const fillForm = async (question: Question) => {
  const programmingData = decodeProgrammingQuestion(question.content)
  const parsed = decodeQuestionContent(question.content)
  const answerData = decodeAnswerData(question.answer)
  questionKind.value = programmingData ? 'programming' : isChoiceType(question.typeName) ? 'choice' : 'fill'
  if (programmingData) {
    programmingLanguage.value = programmingData.language
    competitionMode.value = programmingData.competitionMode
    timeLimitMs.value = programmingData.timeLimitMs
    memoryLimitMb.value = programmingData.memoryLimitMb
    starterCode.value = programmingData.starterCode
    programmingTestCases.value = programmingData.testCases.length ? programmingData.testCases : [{ input: '', expectedOutput: '', isSample: false }]
  }
  options.value = parsed.options.length ? parsed.options : [{ text: '' }, { text: '' }]
  correctOptions.value = (question.answer || '').toUpperCase().match(/[A-Z]/g)?.map((label) => label.charCodeAt(0) - 65).filter((index) => index < options.value.length) || []
  multipleChoice.value = parsed.multiple
  fillAnswers.value = answerData.values.length ? answerData.values : ['']
  fillAnswerMode.value = answerData.mode
  Object.assign(form, {
    subjectId: question.subjectId,
    typeId: question.typeId,
    content: programmingData?.statement ?? parsed.stem,
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
  const name = questionKind.value === 'choice' ? '选择题' : questionKind.value === 'fill' ? '填空题' : '编程题'
  const typeList = questionTypes.value.length ? questionTypes.value : await listQuestionTypes(subjectId)
  const existingType = typeList.find((type) => questionKind.value === 'choice'
    ? isChoiceType(type.name)
    : questionKind.value === 'fill' ? /填空|fill/i.test(type.name) : /编程|程序|programming|coding/i.test(type.name))
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

const toggleCorrect = (index: number) => {
  if (multipleChoice.value) {
    correctOptions.value = correctOptions.value.includes(index)
      ? correctOptions.value.filter((item) => item !== index)
      : [...correctOptions.value, index]
  } else {
    correctOptions.value = correctOptions.value.includes(index) ? [] : [index]
  }
}

const setQuestionKind = (kind: 'choice' | 'fill' | 'programming') => { questionKind.value = kind }

const runProgrammingTestCases = async () => {
  const testCases = programmingTestCases.value
  if (!testCases.length) {
    ElMessage.warning('请至少添加一个测试点')
    return
  }
  runningCode.value = true
  programmingRunResults.value = []
  try {
    for (const testCase of testCases) {
      const result = await executeCode({
        sourceCode: starterCode.value,
        language: programmingLanguage.value,
        input: testCase.input,
        expectedOutput: testCase.expectedOutput,
        timeLimitMs: competitionMode.value ? timeLimitMs.value : undefined,
        memoryLimitMb: competitionMode.value ? memoryLimitMb.value : undefined,
      })
      programmingRunResults.value.push(result)
    }
  } catch {
    ElMessage.error('代码执行失败，请检查本机编译器和运行时配置')
  } finally {
    runningCode.value = false
  }
}

const handleMultipleModeChange = (enabled: string | number | boolean) => {
  if (!enabled && correctOptions.value.length > 1) correctOptions.value = correctOptions.value.slice(0, 1)
}

const removeOption = (index: number) => {
  if (options.value.length <= 1) {
    ElMessage.warning('至少保留一个选项')
    return
  }
  options.value.splice(index, 1)
  correctOptions.value = correctOptions.value.filter((item) => item !== index).map((item) => item > index ? item - 1 : item)
}

const removeFillAnswer = (index: number) => {
  if (fillAnswers.value.length <= 1) {
    ElMessage.warning('至少保留一个答案框')
    return
  }
  fillAnswers.value.splice(index, 1)
}

const setFillAnswerRef = (index: number, element: any) => { fillAnswerRefs.value[index] = element }

const handleSubmit = async () => {
  await formRef.value?.validate()

  if (questionKind.value === 'programming') {
    if (programmingTestCases.value.length > 20) {
      ElMessage.warning('编程题最多支持 20 个测试点')
      return
    }
    const content = encodeProgrammingQuestion({ statement: form.content, language: programmingLanguage.value, starterCode: starterCode.value, testCases: programmingTestCases.value, competitionMode: competitionMode.value, timeLimitMs: timeLimitMs.value, memoryLimitMb: memoryLimitMb.value })
    if (content.length > MAX_PROGRAMMING_QUESTION_LENGTH) {
      ElMessage.warning('编程题内容超过允许上限，请减少题面、代码或测试点数据')
      return
    }
  }

  submitting.value = true
  try {
    const subjectId = await ensureSubjectId()
    const typeId = await ensureTypeId(subjectId)
    const tagIds = await ensureTagIds()

    const payload = {
      subjectId,
      typeId,
      content: questionKind.value === 'choice'
        ? encodeQuestionContent({ stem: form.content, options: options.value, multiple: multipleChoice.value })
        : questionKind.value === 'programming'
          ? encodeProgrammingQuestion({ statement: form.content, language: programmingLanguage.value, starterCode: starterCode.value, testCases: programmingTestCases.value, competitionMode: competitionMode.value, timeLimitMs: timeLimitMs.value, memoryLimitMb: memoryLimitMb.value })
          : form.content.trim(),
      answer: questionKind.value === 'programming' ? null : questionKind.value === 'choice'
        ? (correctOptions.value.map((index) => String.fromCharCode(65 + index)).join(',') || null)
        : encodeAnswers(fillAnswers.value.map((answer) => answer.trim()), fillAnswerMode.value),
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
.choice-option-list { display: grid; gap: 8px; width: 100%; max-width: 760px; }
.choice-option-row { display: flex; align-items: center; gap: 10px; }
.choice-option-row .el-input { flex: 1; }
.option-letter { width: 34px; height: 34px; flex: 0 0 34px; border: 1px solid #d1d9e0; border-radius: 50%; background: #fff; color: #59636e; cursor: pointer; font-weight: 600; }
.option-letter.selected { border-color: #67c23a; background: #67c23a; color: #fff; }
.option-letter.multiple { border-radius: 8px; }
.option-letter.add-option { border-style: dashed; color: #409eff; font-size: 20px; }
.add-option-row { min-height: 36px; }
.fill-answer-row { display: flex; gap: 8px; width: 100%; align-items: flex-start; }
.fill-answer-row > :first-child { flex: 1; }
.program-test-list, .program-run-panel { display: grid; gap: 12px; width: 100%; }
.program-test-case { padding: 12px; border: 1px solid #d1d9e0; border-radius: 6px; }
.program-test-heading { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.program-test-actions { display: flex; align-items: center; gap: 12px; }
.program-test-fields { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
.program-limit-input { display: flex; align-items: center; gap: 10px; }
.program-limit-input .el-input-number { width: 220px; }
.program-run-result { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; color: #59636e; }
.program-run-result pre { width: 100%; max-height: 180px; margin: 0; padding: 10px; overflow: auto; background: #f6f8fa; border-radius: 4px; white-space: pre-wrap; }
@media (max-width: 700px) { .program-test-fields { grid-template-columns: 1fr; } }

</style>
