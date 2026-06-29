<template>
  <main class="page-shell">
    <section class="toolbar">
      <el-form :model="filters" inline label-width="72px" class="filter-form">
        <el-form-item label="科目">
          <el-select
            v-model="filters.subjectId"
            clearable
            filterable
            placeholder="全部科目"
            class="filter-control"
            @change="handleSubjectFilterChange"
            @clear="handleSubjectFilterClear"
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
          <el-select
            v-model="filters.typeId"
            :disabled="!filters.subjectId"
            :loading="typeLoading"
            clearable
            filterable
            placeholder="全部题型"
            class="filter-control"
          >
            <el-option
              v-for="type in questionTypes"
              :key="type.id"
              :label="type.name"
              :value="type.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="标签">
          <el-select
            v-model="filters.tagId"
            clearable
            filterable
            placeholder="全部标签"
            class="filter-control"
          >
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
          <el-button :icon="Plus" @click="openSubjectDialog">新增科目</el-button>
          <el-button
            type="danger"
            :icon="Delete"
            :disabled="!filters.subjectId"
            @click="handleDeleteSubject"
          >
            删除科目
          </el-button>
          <el-button :icon="Plus" @click="openTagDialog">新增标签</el-button>
          <el-button
            type="danger"
            :icon="Delete"
            :disabled="!filters.tagId"
            @click="handleDeleteTag"
          >
            删除标签
          </el-button>
          <el-button type="success" :icon="Plus" @click="dialogVisible = true">新增题目</el-button>
        </el-form-item>
      </el-form>
    </section>

    <el-table
      v-loading="loading"
      :data="questions"
      border
      row-key="id"
      class="question-table"
    >
      <el-table-column prop="subjectName" label="科目" min-width="110" />
      <el-table-column prop="typeName" label="题型" min-width="110" />
      <el-table-column label="题目内容" min-width="260">
        <template #default="{ row }">
          <LatexRenderer :content="row.content" />
        </template>
      </el-table-column>
      <el-table-column label="答案" min-width="220">
        <template #default="{ row }">
          <LatexRenderer :content="row.answer" />
        </template>
      </el-table-column>
      <el-table-column label="解析" min-width="220">
        <template #default="{ row }">
          <LatexRenderer :content="row.analysis" />
        </template>
      </el-table-column>
      <el-table-column label="难度" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="difficultyType(row.difficulty)" effect="plain">{{ row.difficulty }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标签" min-width="180">
        <template #default="{ row }">
          <div class="tag-list">
            <el-tag v-for="tag in row.tags" :key="tag.id" size="small">{{ tag.name }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="图片" min-width="160">
        <template #default="{ row }">
          <div class="image-list">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              class="question-image"
              preview-teleported
            />
            <el-image
              v-if="row.answerImageUrl"
              :src="row.answerImageUrl"
              :preview-src-list="[row.answerImageUrl]"
              fit="cover"
              class="question-image"
              preview-teleported
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="96" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" link :icon="Delete" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-row">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadQuestions"
        @size-change="handleSizeChange"
      />
    </div>

    <QuestionFormDialog v-model="dialogVisible" @success="handleQuestionCreated" />

    <el-dialog v-model="subjectDialogVisible" title="新增科目" width="420px" destroy-on-close>
      <el-form label-width="96px" @submit.prevent>
        <el-form-item label="科目名称" required>
          <el-input
            v-model="subjectForm.name"
            maxlength="50"
            clearable
            placeholder="请输入科目名称"
            @keyup.enter="submitSubject"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subjectDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="subjectSubmitting" @click="submitSubject">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tagDialogVisible" title="新增标签" width="420px" destroy-on-close>
      <el-form label-width="96px" @submit.prevent>
        <el-form-item label="标签名称" required>
          <el-input
            v-model="tagForm.name"
            maxlength="50"
            clearable
            placeholder="请输入标签名称"
            @keyup.enter="submitTag"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="tagSubmitting" @click="submitTag">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup lang="ts">
import { Delete, Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'

import { deleteQuestion, pageQuestions } from '@/api/question'
import { listQuestionTypes } from '@/api/questionType'
import { addSubject, deleteSubject, listSubjects } from '@/api/subject'
import { addTag, deleteTag, listTags } from '@/api/tag'
import LatexRenderer from '@/components/LatexRenderer.vue'
import QuestionFormDialog from '@/components/QuestionFormDialog.vue'
import type { Question } from '@/types/question'
import type { QuestionType } from '@/types/questionType'
import type { Subject } from '@/types/subject'
import type { Tag } from '@/types/tag'

const loading = ref(false)
const typeLoading = ref(false)
const dialogVisible = ref(false)
const subjectDialogVisible = ref(false)
const tagDialogVisible = ref(false)
const subjectSubmitting = ref(false)
const tagSubmitting = ref(false)
const subjects = ref<Subject[]>([])
const questionTypes = ref<QuestionType[]>([])
const tags = ref<Tag[]>([])
const questions = ref<Question[]>([])

const subjectForm = reactive({
  name: '',
})

const tagForm = reactive({
  name: '',
})

const filters = reactive<{
  subjectId?: number
  typeId?: number
  tagId?: number
}>({})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const difficultyType = (difficulty: number) => {
  if (difficulty === 1) {
    return 'success'
  }
  if (difficulty === 2) {
    return 'warning'
  }
  return 'danger'
}

const loadBaseData = async () => {
  const [subjectList, tagList] = await Promise.all([listSubjects(), listTags()])
  subjects.value = subjectList
  tags.value = tagList
}

const loadQuestionTypes = async (subjectId: number) => {
  typeLoading.value = true
  try {
    questionTypes.value = await listQuestionTypes(subjectId)
  } finally {
    typeLoading.value = false
  }
}

const loadQuestions = async () => {
  loading.value = true
  try {
    const result = await pageQuestions({
      page: pagination.page,
      size: pagination.size,
      subjectId: filters.subjectId,
      typeId: filters.typeId,
      tagId: filters.tagId,
    })
    questions.value = result.records
    pagination.total = result.total
  } finally {
    loading.value = false
  }
}

const openSubjectDialog = () => {
  subjectForm.name = ''
  subjectDialogVisible.value = true
}

const openTagDialog = () => {
  tagForm.name = ''
  tagDialogVisible.value = true
}

const submitSubject = async () => {
  const name = subjectForm.name.trim()
  if (!name) {
    ElMessage.warning('请输入科目名称')
    return
  }

  subjectSubmitting.value = true
  try {
    const id = await addSubject({ name })
    ElMessage.success('新增科目成功')
    subjectDialogVisible.value = false
    await loadBaseData()
    filters.subjectId = id
    filters.typeId = undefined
    questionTypes.value = []
  } finally {
    subjectSubmitting.value = false
  }
}

const submitTag = async () => {
  const name = tagForm.name.trim()
  if (!name) {
    ElMessage.warning('请输入标签名称')
    return
  }

  tagSubmitting.value = true
  try {
    const id = await addTag({ name })
    ElMessage.success('新增标签成功')
    tagDialogVisible.value = false
    await loadBaseData()
    filters.tagId = id
  } finally {
    tagSubmitting.value = false
  }
}

const handleSubjectFilterChange = async (subjectId?: number) => {
  filters.typeId = undefined
  questionTypes.value = []

  if (subjectId) {
    await loadQuestionTypes(subjectId)
  }
}

const handleSubjectFilterClear = () => {
  filters.subjectId = undefined
  filters.typeId = undefined
  questionTypes.value = []
}

const handleSearch = () => {
  pagination.page = 1
  loadQuestions()
}

const handleReset = () => {
  filters.subjectId = undefined
  filters.typeId = undefined
  filters.tagId = undefined
  questionTypes.value = []
  pagination.page = 1
  loadQuestions()
}

const handleSizeChange = () => {
  pagination.page = 1
  loadQuestions()
}

const handleQuestionCreated = async () => {
  pagination.page = 1
  await Promise.all([loadBaseData(), loadQuestions()])
}

const handleDeleteSubject = async () => {
  if (!filters.subjectId) {
    return
  }

  const subject = subjects.value.find((item) => item.id === filters.subjectId)
  await ElMessageBox.confirm(`确定删除科目“${subject?.name ?? filters.subjectId}”吗？`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  })

  await deleteSubject(filters.subjectId)
  ElMessage.success('删除科目成功')
  filters.subjectId = undefined
  filters.typeId = undefined
  questionTypes.value = []
  pagination.page = 1
  await Promise.all([loadBaseData(), loadQuestions()])
}

const handleDeleteTag = async () => {
  if (!filters.tagId) {
    return
  }

  const tag = tags.value.find((item) => item.id === filters.tagId)
  await ElMessageBox.confirm(`确定删除标签“${tag?.name ?? filters.tagId}”吗？`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  })

  await deleteTag(filters.tagId)
  ElMessage.success('删除标签成功')
  filters.tagId = undefined
  pagination.page = 1
  await Promise.all([loadBaseData(), loadQuestions()])
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确定删除这道题目吗？', '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  })

  await deleteQuestion(id)
  ElMessage.success('删除题目成功')

  if (questions.value.length === 1 && pagination.page > 1) {
    pagination.page -= 1
  }
  await loadQuestions()
}

onMounted(async () => {
  await loadBaseData()
  await loadQuestions()
})
</script>
