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
        <div class="type-list">
          <div
            v-for="item in dictTypes"
            :key="item.id"
            :class="['type-item', { active: activeType === item.id }]"
            @click="selectType(item)"
          >
            <span class="type-name">{{ item.name }}</span>
            <span class="type-key">{{ item.key }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧字典数据 -->
      <div class="dict-data">
        <div class="toolbar">
          <span class="current-type">当前类型：{{ currentType?.name || '-' }}</span>
          <el-button type="primary" size="small" @click="showDataDialog()" :disabled="!currentType">➕ 新增数据</el-button>
        </div>

        <el-table :data="currentData">
          <el-table-column prop="label" label="字典标签" min-width="140" />
          <el-table-column prop="value" label="字典值" width="120" />
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="ElMessage.success('状态已更新')" />
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
      <el-form :model="typeForm" label-width="80px">
        <el-form-item label="类型名称" required>
          <el-input v-model="typeForm.name" placeholder="请输入类型名称" />
        </el-form-item>
        <el-form-item label="类型标识" required>
          <el-input v-model="typeForm.key" placeholder="请输入类型标识" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="ElMessage.success('保存成功'); typeDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog v-model="dataDialogVisible" :title="isEditData ? '编辑字典数据' : '新增字典数据'" width="450px">
      <el-form :model="dataForm" label-width="80px">
        <el-form-item label="字典标签" required>
          <el-input v-model="dataForm.label" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" required>
          <el-input v-model="dataForm.value" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dataForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dataForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="ElMessage.success('保存成功'); dataDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const dictTypes = ref([
  { id: 1, name: '用户性别', key: 'sys_user_sex', remark: '用户性别列表' },
  { id: 2, name: '系统状态', key: 'sys_normal_disable', remark: '系统状态列表' },
  { id: 3, name: '事项状态', key: 'item_status', remark: '事项状态列表' },
  { id: 4, name: '审批结果', key: 'approval_result', remark: '审批结果列表' },
  { id: 5, name: '证件类型', key: 'cert_type', remark: '证件类型列表' }
])

const dictDataMap = {
  1: [
    { label: '男', value: '0', sort: 1, status: 1, remark: '' },
    { label: '女', value: '1', sort: 2, status: 1, remark: '' },
    { label: '未知', value: '2', sort: 3, status: 1, remark: '' }
  ],
  2: [
    { label: '正常', value: '0', sort: 1, status: 1, remark: '' },
    { label: '停用', value: '1', sort: 2, status: 1, remark: '' }
  ],
  3: [
    { label: '已启用', value: '0', sort: 1, status: 1, remark: '' },
    { label: '已停用', value: '1', sort: 2, status: 1, remark: '' },
    { label: '草稿', value: '2', sort: 3, status: 1, remark: '' }
  ],
  4: [
    { label: '通过', value: '0', sort: 1, status: 1, remark: '' },
    { label: '驳回', value: '1', sort: 2, status: 1, remark: '' },
    { label: '退回', value: '2', sort: 3, status: 1, remark: '' }
  ],
  5: [
    { label: '身份证', value: '0', sort: 1, status: 1, remark: '' },
    { label: '护照', value: '1', sort: 2, status: 1, remark: '' },
    { label: '营业执照', value: '2', sort: 3, status: 1, remark: '' }
  ]
}

const activeType = ref(1)
const currentType = computed(() => dictTypes.value.find(t => t.id === activeType.value))
const currentData = computed(() => dictDataMap[activeType.value] || [])

function selectType(item) { activeType.value = item.id }

const typeDialogVisible = ref(false)
const isEditType = ref(false)
const typeForm = reactive({ name: '', key: '', remark: '' })

function showTypeDialog(row) {
  isEditType.value = !!row
  Object.assign(typeForm, row ? { name: row.name, key: row.key, remark: row.remark } : { name: '', key: '', remark: '' })
  typeDialogVisible.value = true
}

const dataDialogVisible = ref(false)
const isEditData = ref(false)
const dataForm = reactive({ label: '', value: '', sort: 0, status: 1, remark: '' })

function showDataDialog(row) {
  isEditData.value = !!row
  Object.assign(dataForm, row ? { label: row.label, value: row.value, sort: row.sort, status: row.status, remark: row.remark } : { label: '', value: '', sort: 0, status: 1, remark: '' })
  dataDialogVisible.value = true
}

function handleDeleteData(row) {
  ElMessageBox.confirm('确定要删除该字典数据吗？', '警告', { type: 'error' }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
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

  &:hover { background: #F3F4F6; }
  &.active { background: #EFF6FF; color: #1E40AF; }

  .type-name { display: block; font-weight: 500; font-size: 14px; }
  .type-key { display: block; font-size: 12px; color: #9CA3AF; margin-top: 4px; }
  &.active .type-key { color: #3B82F6; }
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
