<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">系统参数</h1>
      <span class="desc">管理系统配置参数</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.configName" placeholder="参数名称" clearable style="width: 180px" />
      <el-input v-model="searchForm.configKey" placeholder="参数键名" clearable style="width: 180px" />
      <el-select v-model="searchForm.configType" placeholder="系统内置" clearable style="width: 140px">
        <el-option label="是" value="Y" />
        <el-option label="否" value="N" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">➕ 新增参数</el-button>
      <span class="total">共 {{ total }} 个参数</span>
    </div>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="configId" label="编号" width="70" />
      <el-table-column prop="configName" label="参数名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="configKey" label="参数键名" width="240" show-overflow-tooltip />
      <el-table-column prop="configValue" label="参数键值" width="180" show-overflow-tooltip />
      <el-table-column label="系统内置" width="100">
        <template #default="{ row }">
          <el-tag :type="row.configType === 'Y' ? 'primary' : 'info'" size="small">
            {{ row.configType === 'Y' ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)" :disabled="row.configType === 'Y'">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑参数' : '新增参数'" width="500px">
      <el-form :model="configForm" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="参数名称" prop="configName" required>
          <el-input v-model="configForm.configName" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey" required>
          <el-input v-model="configForm.configKey" placeholder="请输入参数键名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="参数键值" prop="configValue" required>
          <el-input v-model="configForm.configValue" placeholder="请输入参数键值" />
        </el-form-item>
        <el-form-item label="系统内置" prop="configType">
          <el-radio-group v-model="configForm.configType">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="configForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { getConfigPage, addConfig, updateConfig, deleteConfig as deleteConfigApi } from '@/api/system'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({ configName: '', configKey: '', configType: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingConfigId = ref(null)
const formRef = ref(null)

const configForm = reactive({
  configName: '',
  configKey: '',
  configValue: '',
  configType: 'N',
  remark: ''
})

const formRules = {
  configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入参数键名', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入参数键值', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      configName: searchForm.configName || undefined,
      configKey: searchForm.configKey || undefined,
      configType: searchForm.configType || undefined
    }
    const res = await getConfigPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载系统参数失败', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function resetSearch() {
  Object.assign(searchForm, { configName: '', configKey: '', configType: '' })
  handleSearch()
}

function showDialog(row) {
  isEdit.value = !!row
  if (row) {
    editingConfigId.value = row.configId
    Object.assign(configForm, {
      configName: row.configName,
      configKey: row.configKey,
      configValue: row.configValue,
      configType: row.configType,
      remark: row.remark || ''
    })
  } else {
    editingConfigId.value = null
    Object.assign(configForm, { configName: '', configKey: '', configValue: '', configType: 'N', remark: '' })
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()

  saving.value = true
  try {
    if (isEdit.value) {
      await updateConfig({ configId: editingConfigId.value, ...configForm })
      ElMessage.success('参数更新成功')
    } else {
      await addConfig(configForm)
      ElMessage.success('参数创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存参数失败', error)
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除参数"${row.configName}"吗？`, '警告', { type: 'error' })
    await deleteConfigApi(row.configId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除参数失败', error)
    }
  }
}
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .total { margin-left: auto; font-size: 13px; color: #909399; } }
.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }
</style>
