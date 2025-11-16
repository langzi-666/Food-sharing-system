<template>
  <div class="search-bar">
    <el-input
      v-model="searchQuery"
      placeholder="搜索美食内容..."
      class="search-input"
      clearable
      @keyup.enter="handleSearch"
    >
      <template #append>
        <el-button @click="handleSearch" :icon="Search" />
      </template>
    </el-input>
    
    <div class="filters" v-if="showFilters">
      <el-select
        v-model="selectedCategory"
        placeholder="选择分类"
        clearable
        class="filter-select"
        @change="handleSearch"
      >
        <el-option
          v-for="category in categories"
          :key="category.id"
          :label="category.name"
          :value="category.id"
        />
      </el-select>
      
      <el-select
        v-model="selectedTag"
        placeholder="选择标签"
        clearable
        class="filter-select"
        @change="handleSearch"
      >
        <el-option
          v-for="tag in tags"
          :key="tag.id"
          :label="tag.name"
          :value="tag.id"
        />
      </el-select>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getCategories } from '@/api/categories.js'
import { getTags } from '@/api/tags.js'

const props = defineProps({
  showFilters: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['search'])

const searchQuery = ref('')
const selectedCategory = ref('')
const selectedTag = ref('')
const categories = ref([])
const tags = ref([])

const loadCategories = async () => {
  try {
    const response = await getCategories()
    categories.value = response.data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadTags = async () => {
  try {
    const response = await getTags()
    tags.value = response.data
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const handleSearch = () => {
  emit('search', {
    q: searchQuery.value,
    categoryId: selectedCategory.value,
    tagId: selectedTag.value
  })
}

onMounted(() => {
  if (props.showFilters) {
    loadCategories()
    loadTags()
  }
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  max-width: 600px;
}

.filters {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.filter-select {
  width: 150px;
}
</style>
