<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">字典管理</h1>
      <span class="desc">管理系统字典数据</span>
    </div>

    <div class="dict-layout">
      <!-- 左侧字典类型 -->
      <div class="dict-types">
        <div class="panel-header">
          <span>字典类型</span>
          <el-button type="primary" link size="small" @click="showTypeDialog()">新增</el-button>
        </div>
        <div class="type-list" v-loading="loadingTypes">
          <div
            v-for="item in dictTypes"
            :key="item.dictId"
            :class="['type-item', { active: activeType === item.dictId }]"
            @click="selectType(item)"
          >
            <span class="type-name">{{ item.dictName }}</span>
            <span class="type-key">{{ item.dictType }}</span>
            <div class="type-actions">
              <el-button type="primary" link size="small" @click.stop="showTypeDialog(item)">编辑</el-button>
              <el-button type="danger" link size="small" @click.stop="handleDeleteType(item)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧字典数据 -->
      <div class="dict-data">
        <div class="toolbar">
          <span class="current-type">当前类型：{{ currentType?.dictName || '-' }}</span>
          <el-button type="primary" size="small" @click="showDataDialog()" :disabled="!currentType">➕ 新增数据</el-button>
        </div>

        <el-table :data="currentData" v-loading="loadingData">
          <el-table-column prop="dictLabel" label="字典标签" min-width="140" />
          <el-table-column prop="dictValue" label="字典值" width="120" />
          <el-table-column prop="dictSort" label="排序" width="80" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-switch v-model="row.status" :active-value="0" :inactive-value="1" @change="handleDataStatusChange(row)" />
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="showDataDialog(row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDeleteData(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 字典类型弹窗 -->
    <el-dialog v-model="typeDialogVisible" :title="isEditType ? '编辑字典类型' : '新增字典类型'" width="450px">
      <el-form :model="typeForm" :rules="typeFormRules" ref="typeFormRef" label-width="80px">
        <el-form-item label="类型名称" prop="dictName" required>
          <el-input v-model="typeForm.dictName" placeholder="请输入类型名称" />
        </el-form-item>
        <el-form-item label="类型标识" prop="dictType" required>
          <el-input v-model="typeForm.dictType" placeholder="请输入类型标识" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveType" :loading="savingType">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog v-model="dataDialogVisible" :title="isEditData ? '编辑字典数据' : '新增字典数据'" width="450px">
      <el-form :model="dataForm" :rules="dataFormRules" ref="dataFormRef" label-width="80px">
        <el-form-item label="字典标签" prop="dictLabel" required>
          <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue" required>
          <el-input v-model="dataForm.dictValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="排序" prop="dictSort">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dataForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveData" :loading="savingData">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDictTypeList, addDictType, updateDictType, deleteDictType,
  getDictDataByType, addDictData, updateDictData, deleteDictData
} from '@/api/system'

const loadingTypes = ref(false)
const loadingData = ref(false)
const savingType = ref(false)
const savingData = ref(false)

const dictTypes = ref([])
const dictDataMap = ref({})

const activeType = ref(null)
const currentType = computed(() => dictTypes.value.find(t => t.dictId === activeType.value))
const currentData = computed(() => dictDataMap.value[activeType.value] || [])

// 字典类型表单
const typeDialogVisible = ref(false)
const isEditType = ref(false)
const editingTypeId = ref(null)
const typeFormRef = ref(null)
const typeForm = reactive({ dictName: '', dictType: '', remark: '' })
const typeFormRules = {
  dictName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入类型标识', trigger: 'blur' }]
}

// 字典数据表单
const dataDialogVisible = ref(false)
const isEditData = ref(false)
const editingDataId = ref(null)
const dataFormRef = ref(null)
const dataForm = reactive({ dictLabel: '', dictValue: '', dictSort: 0, status: 0, remark: '' })
const dataFormRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

onMounted(() => {
  loadDictTypes()
})

async function loadDictTypes() {
  loadingTypes.value = true
  try {
    const res = await getDictTypeList()
    dictTypes.value = res.data || []
    if (dictTypes.value.length > 0 && !activeType.value) {
      selectType(dictTypes.value[0])
    }
  } catch (error) {
    console.error('加载字典类型失败', error)
  } finally {
    loadingTypes.value = false
  }
}

async function loadDictData(dictType) {
  loadingData.value = true
  try {
    const res = await getDictDataByType(dictType)
    dictDataMap.value[activeType.value] = res.data || []
  } catch (error) {
    console.error('加载字典数据失败', error)
  } finally {
    loadingData.value = false
  }
}

function selectType(item) {
  activeType.value = item.dictId
  if (!dictDataMap.value[item.dictId]) {
    loadDictData(item.dictType)
  }
}

// 字典类型操作
function showTypeDialog(row) {
  isEditType.value = !!row
  if (row) {
    editingTypeId.value = row.dictId
    Object.assign(typeForm, { dictName: row.dictName, dictType: row.dictType, remark: row.remark || '' })
  } else {
    editingTypeId.value = null
    Object.assign(typeForm, { dictName: '', dictType: '', remark: '' })
  }
  typeDialogVisible.value = true
}

async function handleSaveType() {
  if (!typeFormRef.value) return
  await typeFormRef.value.validate()

  savingType.value = true
  try {
    if (isEditType.value) {
      await updateDictType({ dictId: editingTypeId.value, ...typeForm })
      ElMessage.success('字典类型更新成功')
    } else {
      await addDictType(typeForm)
      ElMessage.success('字典类型创建成功')
    }
    typeDialogVisible.value = false
    loadDictTypes()
  } catch (error) {
    console.error('保存字典类型失败', error)
  } finally {
    savingType.value = false
  }
}

async function handleDeleteType(row) {
  try {
    await ElMessageBox.confirm(`确定要删除字典类型"${row.dictName}"吗？`, '警告', { type: 'error' })
    await deleteDictType(row.dictId)
    ElMessage.success('删除成功')
    if (activeType.value === row.dictId) {
      activeType.value = null
    }
    loadDictTypes()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除字典类型失败', error)
    }
  }
}

// 字典数据操作
function showDataDialog(row) {
  isEditData.value = !!row
  if (row) {
    editingDataId.value = row.dictCode
    Object.assign(dataForm, {
      dictLabel: row.dictLabel,
      dictValue: row.dictValue,
      dictSort: row.dictSort,
      status: row.status,
      remark: row.remark || ''
    })
  } else {
    editingDataId.value = null
    Object.assign(dataForm, { dictLabel: '', dictValue: '', dictSort: 0, status: 0, remark: '' })
  }
  dataDialogVisible.value = true
}

async function handleSaveData() {
  if (!dataFormRef.value) return
  await dataFormRef.value.validate()

  savingData.value = true
  try {
    const submitData = { ...dataForm, dictType: currentType.value.dictType }
    if (isEditData.value) {
      await updateDictData({ dictCode: editingDataId.value, ...submitData })
      ElMessage.success('字典数据更新成功')
    } else {
      await addDictData(submitData)
      ElMessage.success('字典数据创建成功')
    }
    dataDialogVisible.value = false
    loadDictData(currentType.value.dictType)
  } catch (error) {
    console.error('保存字典数据失败', error)
  } finally {
    savingData.value = false
  }
}

async function handleDeleteData(row) {
  try {
    await ElMessageBox.confirm('确定要删除该字典数据吗？', '警告', { type: 'error' })
    await deleteDictData(row.dictCode)
    ElMessage.success('删除成功')
    loadDictData(currentType.value.dictType)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除字典数据失败', error)
    }
  }
}

async function handleDataStatusChange(row) {
  try {
    await updateDictData({ dictCode: row.dictCode, status: row.status })
    ElMessage.success('状态已更新')
  } catch (error) {
    row.status = row.status === 0 ? 1 : 0
    console.error('修改状态失败', error)
  }
}
</script>

<style lang="scss" scoped>
.dict-layout {
  display: flex;
  gap: 20px;
  height: calc(100vh - 240px);
}

.dict-types {
  width: 280px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #F3F4F6;
  font-weight: 600;
  font-size: 14px;
}

.type-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.type-item {
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &:hover {
    background: #F3F4F6;
    .type-actions { opacity: 1; }
  }
  &.active { background: #EFF6FF; color: #1E40AF; }

  .type-name { display: block; font-weight: 500; font-size: 14px; }
  .type-key { display: block; font-size: 12px; color: #9CA3AF; margin-top: 4px; }
  &.active .type-key { color: #3B82F6; }

  .type-actions {
    position: absolute;
    right: 8px;
    top: 50%;
    transform: translateY(-50%);
    opacity: 0;
    transition: opacity 0.2s;
  }
}

.dict-data {
  flex: 1;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  padding: 20px;
  overflow: auto;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  .current-type { font-size: 14px; font-weight: 500; color: #1F2937; }
}
</style>
