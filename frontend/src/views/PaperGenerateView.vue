<template>
  <main class="page-shell">
    <section class="paper-layout">
      <aside class="paper-panel">
        <div class="panel-header">
          <h2>生成试卷</h2>
          <el-button :icon="RefreshLeft" @click="resetCounts">清空</el-button>
        </div>

        <el-empty v-if="!subjects.length" description="暂无科目" />
        <div v-else class="subject-count-list">
          <div v-for="subject in subjects" :key="subject.id" class="subject-count-row">
            <span class="subject-name">{{ subject.name }}</span>
            <el-input-number
              v-model="subjectCountMap[subject.id]"
              :min="0"
              :step="1"
              step-strictly
              controls-position="right"
            />
          </div>
        </div>

        <el-button
          type="primary"
          :icon="DocumentAdd"
          :loading="generating"
          class="generate-button"
          @click="handleGenerate"
        >
          生成试卷
        </el-button>
      </aside>

      <section class="paper-result">
        <el-empty v-if="!paper" description="尚未生成试卷" />
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
                <LatexRenderer :content="question.content" />
                <el-image
                  v-if="question.imageUrl"
                  :src="question.imageUrl"
                  :preview-src-list="[question.imageUrl]"
                  fit="cover"
                  class="preview-image"
                  preview-teleported
                />
              </section>

              <section class="question-block">
                <h3>答案</h3>
                <LatexRenderer :content="question.answer" />
                <el-image
                  v-if="question.answerImageUrl"
                  :src="question.answerImageUrl"
                  :preview-src-list="[question.answerImageUrl]"
                  fit="cover"
                  class="preview-image"
                  preview-teleported
                />
              </section>

              <section v-if="question.analysis" class="question-block">
                <h3>解析</h3>
                <LatexRenderer :content="question.analysis" />
              </section>
            </article>
          </div>
        </template>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { DocumentAdd, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'

import { generatePaper } from '@/api/paper'
import { listSubjects } from '@/api/subject'
import LatexRenderer from '@/components/LatexRenderer.vue'
import type { PaperGenerateResponse } from '@/types/paper'
import type { Subject } from '@/types/subject'

const subjects = ref<Subject[]>([])
const paper = ref<PaperGenerateResponse>()
const generating = ref(false)
const subjectCountMap = reactive<Record<number, number>>({})

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
  subjects.value.forEach((subject) => {
    subjectCountMap[subject.id] = subjectCountMap[subject.id] ?? 0
  })
}

const resetCounts = () => {
  subjects.value.forEach((subject) => {
    subjectCountMap[subject.id] = 0
  })
}

const handleGenerate = async () => {
  const payload: Record<number, number> = {}

  subjects.value.forEach((subject) => {
    const count = subjectCountMap[subject.id] || 0
    if (count > 0) {
      payload[subject.id] = count
    }
  })

  if (!Object.keys(payload).length) {
    ElMessage.warning('请至少为一个科目填写抽题数量')
    return
  }

  generating.value = true
  try {
    paper.value = await generatePaper({ subjectCountMap: payload })
  } finally {
    generating.value = false
  }
}

onMounted(loadSubjects)
</script>
