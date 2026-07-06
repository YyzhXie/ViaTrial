<template>
  <main class="page-shell">
    <section class="paper-layout">
      <aside class="paper-panel">
        <div class="panel-header">
          <h2>预览试卷</h2>
          <el-button :icon="RefreshLeft" @click="resetCounts">清空</el-button>
        </div>

        <el-empty v-if="!subjects.length" description="暂无科目" />
        <template v-else>
          <el-form label-position="top" class="paper-config-form">
            <el-form-item label="组卷科目">
              <el-select
                v-model="selectedSubjectId"
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
          </el-form>

          <el-empty v-if="selectedSubjectId && !questionTypes.length" description="该科目暂无题型" />
          <div v-else-if="questionTypes.length" class="subject-count-list">
            <div v-for="type in questionTypes" :key="type.id" class="subject-count-row">
              <span class="subject-name">{{ type.name }}</span>
              <el-input-number
                v-model="typeCountMap[type.id]"
                :min="0"
                :step="1"
                step-strictly
                controls-position="right"
              />
            </div>
          </div>
        </template>

        <el-button
          type="primary"
          :icon="View"
          :loading="generating"
          class="generate-button"
          @click="handleGenerate"
        >
          预览试卷
        </el-button>
      </aside>

      <section class="paper-result">
        <el-empty v-if="!paper" description="尚未预览试卷" />
        <template v-else>
          <div class="result-summary">
            <el-statistic title="试卷编号" :value="paper.paperId" />
            <el-statistic title="请求题数" :value="paper.totalRequested" />
            <el-statistic title="实际题数" :value="paper.totalActual" />
          </div>

          <div v-if="paper.warnings.length" class="warning-list">
            <el-alert
              v-for="warning in paper.warnings"
              :key="warning"
              :title="warning"
              type="warning"
              show-icon
              :closable="false"
            />
          </div>

          <template v-if="paperMode === 'preview'">
            <div class="paper-action-row">
              <el-button
                type="success"
                :icon="EditPen"
                :disabled="!paper.questions.length"
                @click="startPractice"
              >
                开始做题
              </el-button>
            </div>

            <div class="paper-question-list">
              <article
                v-for="(question, index) in paper.questions"
                :key="question.id"
                class="paper-question"
              >
                <header class="paper-question-header">
                  <span class="question-index">第 {{ index + 1 }} 题</span>
                  <el-tag effect="plain">{{ question.subjectName }}</el-tag>
                  <el-tag effect="plain" type="info">{{ question.typeName }}</el-tag>
                  <el-tag :type="difficultyType(question.difficulty)" effect="plain">
                    难度 {{ question.difficulty }}
                  </el-tag>
                  <el-tag v-for="tag in question.tags" :key="tag.id" size="small">
                    {{ tag.name }}
                  </el-tag>
                </header>

                <section class="question-block">
                  <h3>题目</h3>
                  <LatexRenderer :content="getPracticeQuestion(question).stem" />
                </section>
              </article>
            </div>
          </template>

          <template v-else>
            <article class="practice-card">
              <header class="practice-header">
                <span class="q-number">
                  第 {{ currentIndex + 1 }} / {{ paper.questions.length }} 题
                </span>
                <span class="q-status" :class="currentStatusClass">{{ currentStatusText }}</span>
              </header>

              <section class="practice-question">
                <LatexRenderer :content="currentPracticeQuestion.stem" />
                <el-image
                  v-if="currentQuestion?.imageUrl"
                  :src="currentQuestion.imageUrl"
                  :preview-src-list="[currentQuestion.imageUrl]"
                  fit="cover"
                  class="preview-image"
                  preview-teleported
                />
              </section>

              <div v-if="currentPracticeQuestion.options.length" class="practice-options">
                <label
                  v-for="option in currentPracticeQuestion.options"
                  :key="option.label"
                  class="practice-option"
                  :class="optionClass(option.label)"
                >
                  <input
                    :type="currentPracticeQuestion.multiple ? 'checkbox' : 'radio'"
                    name="practice-option"
                    :value="option.label"
                    :checked="isOptionSelected(option.label)"
                    :disabled="submitted"
                    @change="toggleOption(option.label)"
                  />
                  <span>{{ option.label }}. {{ option.text }}</span>
                </label>
              </div>
              <el-input
                v-else
                v-model="textAnswers[currentIndex]"
                type="textarea"
                :rows="4"
                :disabled="submitted"
                placeholder="请输入答案"
                class="text-answer"
                @input="handleTextAnswer"
              />

              <div v-if="submitted && !isCurrentCorrect" class="answer-tip">
                参考答案：{{ currentQuestion?.answer || '暂无参考答案' }}
              </div>
              <div v-if="submitted && currentQuestion?.analysis" class="answer-analysis">
                <h3>解析</h3>
                <LatexRenderer :content="currentQuestion.analysis" />
              </div>
            </article>

            <div class="practice-nav">
              <el-button :disabled="currentIndex === 0" @click="goPrev">上一题</el-button>
              <span class="page-info">{{ currentIndex + 1 }} / {{ paper.questions.length }}</span>
              <el-button :disabled="currentIndex === paper.questions.length - 1" @click="goNext">
                下一题
              </el-button>
            </div>

            <section class="answer-card">
              <div class="answer-card-title">
                <span>答题卡</span>
                <span>已答 {{ answeredCount }} / {{ paper.questions.length }}</span>
              </div>
              <div class="answer-card-grid">
                <button
                  v-for="(question, index) in paper.questions"
                  :key="question.id"
                  type="button"
                  class="answer-card-num"
                  :class="cardClass(index)"
                  @click="jumpToQuestion(index)"
                >
                  {{ index + 1 }}
                </button>
              </div>
            </section>

            <div class="practice-actions">
              <el-button :icon="RefreshLeft" @click="restartPractice">重新做题</el-button>
              <el-button type="primary" :icon="Finished" :disabled="submitted" @click="submitPaper">
                交卷
              </el-button>
            </div>

            <el-alert
              v-if="resultText"
              :title="resultText"
              type="success"
              show-icon
              :closable="false"
              class="result-message"
            />
          </template>
        </template>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { EditPen, Finished, RefreshLeft, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'

import { generatePaper } from '@/api/paper'
import { listQuestionTypes } from '@/api/questionType'
import { listSubjects } from '@/api/subject'
import LatexRenderer from '@/components/LatexRenderer.vue'
import type { PaperGenerateResponse, PaperQuestion } from '@/types/paper'
import type { QuestionType } from '@/types/questionType'
import type { Subject } from '@/types/subject'

interface PracticeOption {
  label: string
  text: string
}

interface PracticeQuestion {
  stem: string
  options: PracticeOption[]
  multiple: boolean
  correctLabels: string[]
}

const subjects = ref<Subject[]>([])
const questionTypes = ref<QuestionType[]>([])
const selectedSubjectId = ref<number>()
const paper = ref<PaperGenerateResponse>()
const paperMode = ref<'preview' | 'practice'>('preview')
const generating = ref(false)
const submitted = ref(false)
const currentIndex = ref(0)
const resultText = ref('')
const typeCountMap = reactive<Record<number, number>>({})
const answers = reactive<Record<number, string[]>>({})
const textAnswers = reactive<Record<number, string>>({})

const difficultyType = (difficulty: number) => {
  if (difficulty === 1) {
    return 'success'
  }
  if (difficulty === 2) {
    return 'warning'
  }
  return 'danger'
}

const loadSubjects = async () => {
  subjects.value = await listSubjects()
}

const resetCounts = () => {
  questionTypes.value.forEach((type) => {
    typeCountMap[type.id] = 0
  })
}

const handleSubjectChange = async (subjectId: number) => {
  questionTypes.value = await listQuestionTypes(subjectId)
  questionTypes.value.forEach((type) => {
    typeCountMap[type.id] = typeCountMap[type.id] ?? 0
  })
}

const handleGenerate = async () => {
  if (!selectedSubjectId.value) {
    ElMessage.warning('请选择组卷科目')
    return
  }

  const payload: Record<number, number> = {}
  questionTypes.value.forEach((type) => {
    const count = typeCountMap[type.id] || 0
    if (count > 0) {
      payload[type.id] = count
    }
  })

  if (!Object.keys(payload).length) {
    ElMessage.warning('请至少为一个题型填写抽题数量')
    return
  }

  generating.value = true
  try {
    paper.value = await generatePaper({
      subjectId: selectedSubjectId.value,
      typeCountMap: payload,
    })
    paperMode.value = 'preview'
    resetPracticeState()
  } finally {
    generating.value = false
  }
}

const currentQuestion = computed(() => paper.value?.questions[currentIndex.value])

const currentPracticeQuestion = computed(() => {
  if (!currentQuestion.value) {
    return { stem: '', options: [], multiple: false, correctLabels: [] }
  }
  return getPracticeQuestion(currentQuestion.value)
})

const answeredCount = computed(() => {
  if (!paper.value) {
    return 0
  }
  return paper.value.questions.filter((question, index) => hasAnswered(question, index)).length
})

const currentStatusText = computed(() => {
  if (!currentQuestion.value) {
    return '未作答'
  }
  if (submitted.value) {
    return isCurrentCorrect.value ? '回答正确' : '回答错误'
  }
  return hasAnswered(currentQuestion.value, currentIndex.value) ? '已作答' : '未作答'
})

const currentStatusClass = computed(() => {
  if (!submitted.value) {
    return hasAnswered(currentQuestion.value, currentIndex.value) ? 'answered' : ''
  }
  return isCurrentCorrect.value ? 'correct' : 'wrong'
})

const isCurrentCorrect = computed(() => {
  if (!currentQuestion.value) {
    return false
  }
  return isAnswerCorrect(currentQuestion.value, currentIndex.value)
})

const normalizeAnswer = (value?: string | null) => {
  return (value || '')
    .trim()
    .toUpperCase()
    .replace(/[，,、；;。\s]/g, '')
}

const uniqueLabels = (labels: string[]) => [...new Set(labels.map((label) => label.toUpperCase()))]

const getPracticeQuestion = (question: PaperQuestion): PracticeQuestion => {
  const parsedOptions = parseOptions(question)
  const correctLabels = getCorrectLabels(question, parsedOptions.options)
  return {
    stem: parsedOptions.stem,
    options: parsedOptions.options,
    multiple: isMultipleQuestion(question, correctLabels),
    correctLabels,
  }
}

const parseOptions = (question: PaperQuestion) => {
  const lines = question.content.split(/\r?\n/)
  const options: PracticeOption[] = []
  const stemLines: string[] = []
  const optionPattern = /^\s*([A-Ha-h])[\.\、\．\:：\s]+(.+)$/

  lines.forEach((line) => {
    const match = line.match(optionPattern)
    if (match) {
      options.push({
        label: match[1].toUpperCase(),
        text: match[2].trim(),
      })
    } else {
      stemLines.push(line)
    }
  })

  if (!options.length && isJudgeQuestion(question)) {
    return {
      stem: question.content,
      options: [
        { label: 'T', text: '正确' },
        { label: 'F', text: '错误' },
      ],
    }
  }

  return {
    stem: stemLines.join('\n').trim() || question.content,
    options,
  }
}

const isJudgeQuestion = (question: PaperQuestion) => {
  return /判断|正误|true|false/i.test(question.typeName)
}

const isMultipleQuestion = (question: PaperQuestion, labels: string[]) => {
  return /多选|多项|multiple/i.test(question.typeName) || labels.length > 1
}

const getCorrectLabels = (question: PaperQuestion, options: PracticeOption[]) => {
  const normalized = normalizeAnswer(question.answer)
  if (!normalized) {
    return []
  }

  if (isJudgeQuestion(question)) {
    if (/^(T|TRUE|正确|对|是)$/.test(normalized)) {
      return ['T']
    }
    if (/^(F|FALSE|错误|错|否)$/.test(normalized)) {
      return ['F']
    }
  }

  const optionLabels = options.map((option) => option.label)
  const labels = uniqueLabels(normalized.match(/[A-H]/g) || [])
    .filter((label) => optionLabels.includes(label))
    .sort()
  return labels
}

const resetPracticeState = () => {
  Object.keys(answers).forEach((key) => {
    delete answers[Number(key)]
  })
  Object.keys(textAnswers).forEach((key) => {
    delete textAnswers[Number(key)]
  })
  currentIndex.value = 0
  submitted.value = false
  resultText.value = ''
}

const startPractice = () => {
  resetPracticeState()
  paperMode.value = 'practice'
}

const restartPractice = () => {
  resetPracticeState()
}

const hasAnswered = (question: PaperQuestion | undefined, index: number) => {
  if (!question) {
    return false
  }
  const practiceQuestion = getPracticeQuestion(question)
  if (practiceQuestion.options.length) {
    return Boolean(answers[index]?.length)
  }
  return Boolean(textAnswers[index]?.trim())
}

const isOptionSelected = (label: string) => {
  return Boolean(answers[currentIndex.value]?.includes(label))
}

const toggleOption = (label: string) => {
  if (submitted.value) {
    return
  }
  const practiceQuestion = currentPracticeQuestion.value
  if (!practiceQuestion.multiple) {
    answers[currentIndex.value] = [label]
    return
  }

  const selected = new Set(answers[currentIndex.value] || [])
  if (selected.has(label)) {
    selected.delete(label)
  } else {
    selected.add(label)
  }
  answers[currentIndex.value] = [...selected].sort()
}

const handleTextAnswer = () => {
  resultText.value = ''
}

const optionClass = (label: string) => {
  const classes: string[] = []
  if (isOptionSelected(label)) {
    classes.push('selected')
  }
  if (submitted.value) {
    if (currentPracticeQuestion.value.correctLabels.includes(label)) {
      classes.push('correct')
    } else if (isOptionSelected(label)) {
      classes.push('wrong')
    }
  }
  return classes
}

const cardClass = (index: number) => {
  const question = paper.value?.questions[index]
  const classes: string[] = []
  if (index === currentIndex.value) {
    classes.push('active')
  }
  if (hasAnswered(question, index)) {
    classes.push('answered')
  }
  if (submitted.value && question && hasAnswered(question, index)) {
    classes.push(isAnswerCorrect(question, index) ? 'correct' : 'wrong')
  }
  return classes
}

const isAnswerCorrect = (question: PaperQuestion, index: number) => {
  const practiceQuestion = getPracticeQuestion(question)
  if (practiceQuestion.options.length) {
    const selected = [...(answers[index] || [])].sort()
    const correct = [...practiceQuestion.correctLabels].sort()
    return Boolean(correct.length) && selected.length === correct.length
      && selected.every((item, itemIndex) => item === correct[itemIndex])
  }
  return normalizeAnswer(textAnswers[index]) === normalizeAnswer(question.answer)
}

const goPrev = () => {
  if (currentIndex.value > 0) {
    currentIndex.value -= 1
  }
}

const goNext = () => {
  if (paper.value && currentIndex.value < paper.value.questions.length - 1) {
    currentIndex.value += 1
  }
}

const jumpToQuestion = (index: number) => {
  currentIndex.value = index
}

const submitPaper = () => {
  if (!paper.value) {
    return
  }
  if (answeredCount.value < paper.value.questions.length) {
    ElMessage.warning('还有题目未作答，请完成所有题目后再交卷')
    return
  }

  submitted.value = true
  const correctCount = paper.value.questions.filter((question, index) => isAnswerCorrect(question, index)).length
  const total = paper.value.questions.length
  const rate = total ? Math.round((correctCount / total) * 100) : 0
  resultText.value = `交卷完成：答对 ${correctCount} / ${total} 题，正确率 ${rate}%`
}

onMounted(loadSubjects)
</script>
