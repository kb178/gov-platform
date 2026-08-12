<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">部门管理</h1>
      <span class="desc">管理组织架构</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.deptName" placeholder="部门名称" clearable style="width: 180px" />
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">➕ 新增部门</el-button>
      <el-button @click="toggleExpand">{{ isExpand ? '折叠全部' : '展开全部' }}</el-button>
      <span class="total">共 {{ deptCount }} 个部门</span>
    </div>

    <el-table :data="tableData" row-key="deptId" :default-expand-all="isExpand" :tree-props="{ children: 'children' }">
      <el-table-column prop="deptName" label="部门名称" min-width="220" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="leader" label="负责人" width="100" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="120" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
          <el-button type="primary" link size="small" @click="showDialog(null, row.deptId)">新增</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="500px">
      <el-form :model="deptForm" label-width="80px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="deptForm.parentId"
            :data="treeData"
            :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
            placeholder="选择上级部门（不选则为顶级）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" required>
          <el-input v-model="deptForm.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="deptForm.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="deptForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="deptForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="deptForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({ deptName: '', status: '' })
const isExpand = ref(true)

const tableData = ref([
  {
    deptId: 1, deptName: '海口市政府', sort: 1, leader: '张市长', phone: '0898-12345678', status: 1, createTime: '2024-01-01',
    children: [
      {
        deptId: 11, deptName: '政务服务中心', sort: 1, leader: '李主任', phone: '0898-12345679', status: 1, createTime: '2024-01-01',
        children: [
          { deptId: 111, deptName: '综合窗口', sort: 1, leader: '王窗口', phone: '0898-12345680', status: 1, createTime: '2024-01-05' },
          { deptId: 112, deptName: '审批科', sort: 2, leader: '赵审批', phone: '0898-12345681', status: 1, createTime: '2024-01-05' }
        ]
      },
      { deptId: 12, deptName: '公安局', sort: 2, leader: '刘局长', phone: '0898-22345678', status: 1, createTime: '2024-01-01' },
      { deptId: 13, deptName: '市场监管局', sort: 3, leader: '陈局长', phone: '0898-32345678', status: 1, createTime: '2024-01-01' },
      { deptId: 14, deptName: '人社局', sort: 4, leader: '杨局长', phone: '0898-42345678', status: 1, createTime: '2024-01-01' },
      { deptId: 15, deptName: '自然资源局', sort: 5, leader: '黄局长', phone: '0898-52345678', status: 1, createTime: '2024-01-01' },
      { deptId: 16, deptName: '公积金中心', sort: 6, leader: '吴主任', phone: '0898-62345678', status: 1, createTime: '2024-01-01' }
    ]
  }
])

const treeData = computed(() => tableData.value)
const deptCount = computed(() => {
  let count = 0
  const walk = (list) => { list.forEach(item => { count++; if (item.children) walk(item.children) }) }
  walk(tableData.value)
  return count
})

function toggleExpand() { isExpand.value = !isExpand.value }

const dialogVisible = ref(false)
const isEdit = ref(false)
const deptForm = reactive({ parentId: null, deptName: '', leader: '', phone: '', sort: 0, status: 1 })

function showDialog(row, parentId) {
  isEdit.value = !!row
  if (row) {
    Object.assign(deptForm, { parentId: null, deptName: row.deptName, leader: row.leader, phone: row.phone, sort: row.sort, status: row.status })
  } else {
    Object.assign(deptForm, { parentId: parentId || null, deptName: '', leader: '', phone: '', sort: 0, status: 1 })
  }
  dialogVisible.value = true
}

function handleSave() {
  if (!deptForm.deptName) return ElMessage.warning('请输入部门名称')
  ElMessage.success(isEdit.value ? '部门更新成功' : '部门创建成功')
  dialogVisible.value = false
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除部门"${row.deptName}"吗？`, '警告', { type: 'error' }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function handleStatusChange(row) { ElMessage.success(`部门已${row.status ? '启用' : '停用'}`) }
function handleSearch() { ElMessage.info('搜索功能待对接接口') }
function resetSearch() { Object.assign(searchForm, { deptName: '', status: '' }) }
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .total { margin-left: auto; font-size: 13px; color: #909399; } }
</style>
