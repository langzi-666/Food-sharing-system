<template>
  <div class="tag-selector">
    <el-select
      v-model="selectedTags"
      multiple
      filterable
      allow-create
      default-first-option
      :reserve-keyword="false"
      placeholder="选择或输入标签"
      @change="handleChange"
    >
      <el-option
        v-for="tag in allTags"
        :key="tag.id"
        :label="tag.name"
        :value="tag.id"
      />
    </el-select>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTags, createTag } from '@/api/tags.js'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const selectedTags = ref(props.modelValue)
const allTags = ref([])

const loadTags = async () => {
  try {
    const response = await getTags()
    allTags.value = response.data
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const handleChange = async (value) => {
  // 处理新创建的标签
  const newTags = []
  for (const item of value) {
    if (typeof item === 'string') {
      // 新标签，需要创建
      try {
        const response = await createTag({ name: item })
        // axios拦截器已经返回了response.data，所以直接使用response
        newTags.push(response?.id)
        allTags.value.push(response)
      } catch (error) {
        console.error('创建标签失败:', error)
      }
    } else {
      newTags.push(item)
    }
  }
  
  selectedTags.value = newTags
  emit('update:modelValue', newTags)
}

onMounted(() => {
  loadTags()
})
</script>

<style scoped>
.tag-selector {
  width: 100%;
}
</style>
