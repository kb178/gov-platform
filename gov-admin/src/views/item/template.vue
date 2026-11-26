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
      <span class="pagination-info">共 {{ total }} 条记录</span>
      <el-pagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="Number(total)"
        layout="prev, pager, next"
        @current-change="handlePageChange"
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
        <el-form-item label="备注" prop="remark">
          <el-input v-model="templateForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="templateForm.status">
            <el-radio :value="0">启用</el-radio>
            <el-radio :value="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="表单配置">
          <div class="form-builder">
            <div class="builder-toolbar">
              <el-button type="primary" plain size="small" @click="addFormField">➕ 添加字段</el-button>
              <el-button size="small" @click="toggleJsonView">{{ showJson ? '可视化编辑' : '查看JSON' }}</el-button>
            </div>

            <!-- JSON 模式 -->
            <el-input v-if="showJson" v-model="templateForm.formConfig" type="textarea" :rows="8" placeholder="JSON格式的表单配置" />

            <!-- 可视化模式 -->
            <div v-else class="field-list">
              <div v-for="(field, index) in formFields" :key="index" class="field-item">
                <div class="field-header">
                  <span class="field-index">字段 {{ index + 1 }}</span>
                  <el-button type="danger" link size="small" @click="removeFormField(index)">删除</el-button>
                </div>
                <el-row :gutter="12">
                  <el-col :span="8">
                    <el-input v-model="field.label" placeholder="字段名称（如：姓名）" size="small" />
                  </el-col>
                  <el-col :span="8">
                    <el-select v-model="field.type" placeholder="字段类型" size="small" style="width: 100%">
                      <el-option label="单行文本" value="input" />
                      <el-option label="多行文本" value="textarea" />
                      <el-option label="数字" value="number" />
                      <el-option label="日期" value="date" />
                      <el-option label="下拉选择" value="select" />
                      <el-option label="单选" value="radio" />
                      <el-option label="文件上传" value="upload" />
                    </el-select>
                  </el-col>
                  <el-col :span="8">
                    <el-checkbox v-model="field.required" size="small">必填</el-checkbox>
                  </el-col>
                </el-row>
                <el-row :gutter="12" style="margin-top: 8px;">
                  <el-col :span="12">
                    <el-input v-model="field.placeholder" placeholder="占位提示文字" size="small" />
                  </el-col>
                  <el-col :span="12" v-if="['select','radio'].includes(field.type)">
                    <el-input v-model="field.options" placeholder="选项，用逗号分隔（如：男,女）" size="small" />
                  </el-col>
                  <el-col :span="12" v-if="field.type === 'upload'">
                    <el-input v-model="field.accept" placeholder="文件格式（如：image/*,.pdf）" size="small" />
                  </el-col>
                  <el-col :span="6" v-if="field.type === 'upload'">
                    <el-input-number v-model="field.maxCount" :min="1" :max="20" size="small" placeholder="最多文件数" style="width: 100%" />
                  </el-col>
                </el-row>
              </div>
              <el-empty v-if="formFields.length === 0" description="还没有字段，点击上方添加" :image-size="60" />
            </div>
          </div>
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
  remark: '',
  status: 0,
  formConfig: ''
})

// 表单构建器
const showJson = ref(false)
const formFields = ref([])

function addFormField() {
  formFields.value.push({ label: '', type: 'input', required: false, placeholder: '', options: '', accept: '', maxCount: 5 })
}

function removeFormField(index) {
  formFields.value.splice(index, 1)
}

function toggleJsonView() {
  if (!showJson.value) {
    // 切到 JSON 模式：把字段列表转成 JSON
    templateForm.formConfig = JSON.stringify(buildConfigFromFields(), null, 2)
  } else {
    // 切回可视化：从 JSON 解析出字段列表
    parseFieldsFromConfig()
  }
  showJson.value = !showJson.value
}

function buildConfigFromFields() {
  const fields = formFields.value.map(f => {
    const item = { name: f.label, type: f.type, required: f.required }
    if (f.placeholder) item.placeholder = f.placeholder
    if (f.options && ['select','radio'].includes(f.type)) {
      item.options = f.options.split(/[,，]/).map(s => s.trim()).filter(Boolean)
    }
    if (f.type === 'upload') {
      if (f.accept) item.accept = f.accept
      if (f.maxCount) item.maxCount = f.maxCount
    }
    return item
  })
  return { fields }
}

function parseFieldsFromConfig() {
  try {
    let parsed = templateForm.formConfig
    if (typeof parsed === 'string') {
      parsed = JSON.parse(parsed || '{}')
    }
    // 兼容 {fields: [...]} 和 [...] 两种格式
    if (parsed && !Array.isArray(parsed) && parsed.fields) {
      parsed = parsed.fields
    }
    if (!Array.isArray(parsed)) {
      parsed = []
    }
    formFields.value = parsed.map(item => ({
      label: item.name || item.label || '',
      type: item.type || 'input',
      required: !!item.required,
      placeholder: item.placeholder || item.hint || '',
      options: Array.isArray(item.options) ? item.options.join(',') : (item.options || ''),
      accept: item.accept || '',
      maxCount: item.maxCount || 5
    }))
  } catch (e) {
    console.warn('解析表单配置失败:', e)
    formFields.value = []
  }
}

// 保存前把字段列表同步到 formConfig
function syncFieldsToConfig() {
  if (!showJson.value) {
    templateForm.formConfig = JSON.stringify(buildConfigFromFields())
  }
}

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
    // 自动选中第一个事项，加载对应模板
    if (itemList.value.length > 0) {
      searchForm.itemId = itemList.value[0].itemId
      loadData()
    }
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

function handlePageChange(page) {
  pageNum.value = page
  loadData()
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
  showJson.value = false
  if (row) {
    editingTemplateId.value = row.templateId
    // formConfig 可能是字符串也可能是对象，统一转字符串
    let config = row.formConfig || ''
    if (typeof config === 'object') config = JSON.stringify(config)
    Object.assign(templateForm, {
      itemId: row.itemId,
      templateName: row.templateName,
      remark: row.remark || '',
      status: row.status,
      formConfig: config
    })
    parseFieldsFromConfig()
  } else {
    editingTemplateId.value = null
    Object.assign(templateForm, { itemId: searchForm.itemId || '', templateName: '', remark: '', status: 0, formConfig: '' })
    formFields.value = []
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()
  syncFieldsToConfig()

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

.form-builder {
  width: 100%;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  padding: 12px;
}
.builder-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.field-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.field-item {
  background: #F9FAFB;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  padding: 12px;
}
.field-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  .field-index { font-size: 12px; color: #6B7280; font-weight: 500; }
}
</style>
