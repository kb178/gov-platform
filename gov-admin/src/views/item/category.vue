<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">事项分类</h1>
      <span class="desc">管理政务服务事项分类</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.categoryName" placeholder="分类名称" clearable style="width: 180px" />
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="0" />
        <el-option label="停用" :value="1" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">➕ 新增分类</el-button>
      <el-button @click="toggleExpand">{{ isExpand ? '折叠全部' : '展开全部' }}</el-button>
      <span class="total">共 {{ categoryCount }} 个分类</span>
    </div>

    <el-table :data="tableData" row-key="categoryId" :default-expand-all="isExpand" :tree-props="{ children: 'children' }" v-loading="loading">
      <el-table-column prop="categoryName" label="分类名称" min-width="200" />
      <el-table-column prop="categoryCode" label="分类编码" width="150" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="0" :inactive-value="1" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
          <el-button type="primary" link size="small" @click="showDialog(null, row.categoryId)">新增</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500px">
      <el-form :model="categoryForm" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="上级分类" prop="parentId">
          <el-tree-select
            v-model="categoryForm.parentId"
            :data="tableData"
            :props="{ label: 'categoryName', value: 'categoryId', children: 'children' }"
            placeholder="选择上级分类（不选则为顶级）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName" required>
          <el-input v-model="categoryForm.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode" required>
          <el-input v-model="categoryForm.categoryCode" placeholder="请输入分类编码" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="categoryForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryTree, addCategory, updateCategory, deleteCategory as deleteCategoryApi } from '@/api/item'

const loading = ref(false)
const saving = ref(false)
const searchForm = reactive({ categoryName: '', status: '' })
const isExpand = ref(true)
const tableData = ref([])
const allData = ref([])

const categoryCount = computed(() => {
  let count = 0
  const walk = (list) => { list.forEach(item => { count++; if (item.children) walk(item.children) }) }
  walk(tableData.value)
  return count
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingCategoryId = ref(null)
const formRef = ref(null)

const categoryForm = reactive({
  parentId: null,
  categoryName: '',
  categoryCode: '',
  sort: 0,
  status: 0,
  remark: ''
})

const formRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  categoryCode: [{ required: true, message: '请输入分类编码', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getCategoryTree()
    allData.value = res.data || []
    filterData()
  } catch (error) {
    console.error('加载分类列表失败', error)
  } finally {
    loading.value = false
  }
}

function filterData() {
  let list = [...allData.value]
  if (searchForm.categoryName) {
    list = filterTree(list, item => item.categoryName.includes(searchForm.categoryName))
  }
  if (searchForm.status !== '') {
    list = filterTree(list, item => item.status === searchForm.status)
  }
  tableData.value = list
}

function filterTree(tree, predicate) {
  return tree.filter(item => {
    if (item.children) {
      item.children = filterTree(item.children, predicate)
    }
    return predicate(item) || (item.children && item.children.length > 0)
  })
}

function toggleExpand() {
  isExpand.value = !isExpand.value
  const data = tableData.value
  tableData.value = []
  setTimeout(() => { tableData.value = data }, 0)
}

function handleSearch() {
  filterData()
}

function resetSearch() {
  Object.assign(searchForm, { categoryName: '', status: '' })
  filterData()
}

function showDialog(row, parentId) {
  isEdit.value = !!row
  if (row) {
    editingCategoryId.value = row.categoryId
    Object.assign(categoryForm, {
      parentId: row.parentId || null,
      categoryName: row.categoryName,
      categoryCode: row.categoryCode,
      sort: row.sort,
      status: row.status,
      remark: row.remark || ''
    })
  } else {
    editingCategoryId.value = null
    Object.assign(categoryForm, { parentId: parentId || null, categoryName: '', categoryCode: '', sort: 0, status: 0, remark: '' })
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()

  saving.value = true
  try {
    if (isEdit.value) {
      await updateCategory({ categoryId: editingCategoryId.value, ...categoryForm })
      ElMessage.success('分类更新成功')
    } else {
      await addCategory(categoryForm)
      ElMessage.success('分类创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存分类失败', error)
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除分类"${row.categoryName}"吗？`, '警告', { type: 'error' })
    await deleteCategoryApi(row.categoryId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败', error)
    }
  }
}

async function handleStatusChange(row) {
  try {
    await updateCategory({ categoryId: row.categoryId, status: row.status })
    ElMessage.success(`分类已${row.status === 0 ? '启用' : '停用'}`)
  } catch (error) {
    row.status = row.status === 0 ? 1 : 0
    console.error('修改状态失败', error)
  }
}
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .total { margin-left: auto; font-size: 13px; color: #909399; } }
</style>
