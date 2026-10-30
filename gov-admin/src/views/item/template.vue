<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">表单模板</h1>
      <span class="desc">管理事项表单模板</span>
    </div>

    <div class="search-bar">
      <el-select v-model="searchForm.itemId" placeholder="选择事项" clearable filterable style="width: 260px">
        <el-option v-for="item in itemList" :key="item.itemId" :label="item.itemName" :value="item.itemId" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">➕ 新增模板</el-button>
      <span class="total">共 {{ total }} 个模板</span>
    </div>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="templateId" label="模板编号" width="100" />
      <el-table-column prop="templateName" label="模板名称" min-width="180" />
      <el-table-column prop="itemName" label="所属事项" min-width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">
            {{ row.status === 0 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="80" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
          <el-button :type="row.status === 0 ? 'warning' : 'success'" link size="small" @click="handleToggleStatus(row)">
            {{ row.status === 0 ? '禁用' : '启用' }}
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <span class="pagination-info">共 {{ total }} 条记录，每页 {{ pageSize }} 条</span>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        layout="prev, pager, next, jumper"
        :total="total"
        @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模板' : '新增模板'" width="700px">
      <el-form :model="templateForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="所属事项" prop="itemId" required>
          <el-select v-model="templateForm.itemId" placeholder="请选择事项" filterable style="width: 100%">
            <el-option v-for="item in itemList" :key="item.itemId" :label="item.itemName" :value="item.itemId" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板名称" prop="templateName" required>
          <el-input v-model="templateForm.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板描述" prop="description">
          <el-input v-model="templateForm.description" type="textarea" :rows="3" placeholder="请输入模板描述" />
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="templateForm.version" placeholder="如：1.0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="templateForm.status">
            <el-radio :value="0">启用</el-radio>
            <el-radio :value="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="表单内容" prop="formContent">
          <el-input v-model="templateForm.formContent" type="textarea" :rows="6" placeholder="请输入JSON格式的表单配置" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFormTemplateByItem, addFormTemplate, updateFormTemplate, deleteFormTemplate, enableFormTemplate, disableFormTemplate } from '@/api/item'
import { getItemList } from '@/api/item'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({ itemId: '' })
const itemList = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingTemplateId = ref(null)
const formRef = ref(null)

const templateForm = reactive({
  itemId: '',
  templateName: '',
  description: '',
  version: '1.0',
  status: 0,
  formContent: ''
})

const formRules = {
  itemId: [{ required: true, message: '请选择事项', trigger: 'change' }],
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }]
}

onMounted(() => {
  loadItemList()
})

async function loadItemList() {
  try {
    const res = await getItemList({ pageNum: 1, pageSize: 1000 })
    itemList.value = res.data.records || []
  } catch (error) {
    console.error('加载事项列表失败', error)
  }
}

async function loadData() {
  if (!searchForm.itemId) {
    tableData.value = []
    total.value = 0
    return
  }

  loading.value = true
  try {
    const res = await getFormTemplateByItem(searchForm.itemId)
    tableData.value = res.data || []
    total.value = tableData.value.length
  } catch (error) {
    console.error('加载模板列表失败', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function resetSearch() {
  searchForm.itemId = ''
  tableData.value = []
  total.value = 0
}

function showDialog(row) {
  isEdit.value = !!row
  if (row) {
    editingTemplateId.value = row.templateId
    Object.assign(templateForm, {
      itemId: row.itemId,
      templateName: row.templateName,
      description: row.description || '',
      version: row.version || '1.0',
      status: row.status,
      formContent: row.formContent || ''
    })
  } else {
    editingTemplateId.value = null
    Object.assign(templateForm, { itemId: searchForm.itemId || '', templateName: '', description: '', version: '1.0', status: 0, formContent: '' })
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()

  saving.value = true
  try {
    if (isEdit.value) {
      await updateFormTemplate({ templateId: editingTemplateId.value, ...templateForm })
      ElMessage.success('模板更新成功')
    } else {
      await addFormTemplate(templateForm)
      ElMessage.success('模板创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存模板失败', error)
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(row) {
  try {
    const action = row.status === 0 ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}模板"${row.templateName}"吗？`, '提示', { type: 'warning' })
    if (row.status === 0) {
      await disableFormTemplate(row.templateId)
    } else {
      await enableFormTemplate(row.templateId)
    }
    ElMessage.success(`模板已${action}`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改状态失败', error)
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除模板"${row.templateName}"吗？删除后不可恢复。`, '警告', { type: 'error' })
    await deleteFormTemplate(row.templateId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除模板失败', error)
    }
  }
}
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .total { margin-left: auto; font-size: 13px; color: #909399; } }
.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }
</style>
