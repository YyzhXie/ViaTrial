<template>
  <el-select
    :model-value="modelValue"
    :loading="loading"
    multiple
    clearable
    filterable
    allow-create
    default-first-option
    placeholder="选择标签"
    class="tag-selector"
    @update:model-value="handleChange"
  >
    <el-option
      v-for="tag in tags"
      :key="tag.id"
      :label="tag.name"
      :value="tag.id"
    />
  </el-select>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

import { listTags } from '@/api/tag'
import type { Tag } from '@/types/tag'

defineProps<{
  modelValue: Array<number | string>
}>()

const emit = defineEmits<{
  'update:modelValue': [value: Array<number | string>]
}>()

const loading = ref(false)
const tags = ref<Tag[]>([])

const loadTags = async () => {
  loading.value = true
  try {
    tags.value = await listTags()
  } finally {
    loading.value = false
  }
}

const handleChange = (value: Array<number | string>) => {
  emit('update:modelValue', value)
}

onMounted(loadTags)
</script>

<style scoped>
.tag-selector {
  width: 100%;
}
</style>
