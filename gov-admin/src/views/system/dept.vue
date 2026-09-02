<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">部门管理</h1>
      <span class="desc">管理组织架构</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.deptName" placeholder="部门名称" clearable style="width: 180px" />
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="0" />
        <el-option label="停用" :value="1" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">➕ 新增部门</el-button>
      <el-button @click="toggleExpand">{{ isExpand ? '折叠全部' : '展开全部' }}</el-button>
      <span class="total">共 {{ deptCount }} 个部门</span>
    </div>

    <el-table :data="tableData" row-key="deptId" :default-expand-all="isExpand" :tree-props="{ children: 'children' }" v-loading="loading">
      <el-table-column prop="deptName" label="部门名称" min-width="220" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="leader" label="负责人" width="100" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="0" :inactive-value="1" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
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
      <el-form :model="deptForm" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="上级部门" prop="parentId">
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
        <el-form-item label="部门名称" prop="deptName" required>
          <el-input v-model="deptForm.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="deptForm.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="deptForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="deptForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="deptForm.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
          </el-radio-group>
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
import { getDeptList, addDept, updateDept, deleteDept as deleteDeptApi } from '@/api/system'

const loading = ref(false)
const saving = ref(false)
const searchForm = reactive({ deptName: '', status: '' })
const isExpand = ref(true)
const tableData = ref([])
const allData = ref([])

const treeData = computed(() => tableData.value)
const deptCount = computed(() => {
  let count = 0
  const walk = (list) => { list.forEach(item => { count++; if (item.children) walk(item.children) }) }
  walk(tableData.value)
  return count
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingDeptId = ref(null)
const formRef = ref(null)

const deptForm = reactive({
  parentId: null,
  deptName: '',
  leader: '',
  phone: '',
  sort: 0,
  status: 0
})

const formRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getDeptList()
    allData.value = res.data || []
    filterData()
  } catch (error) {
    console.error('加载部门列表失败', error)
  } finally {
    loading.value = false
  }
}

function filterData() {
  let list = [...allData.value]
  if (searchForm.deptName) {
    list = filterTree(list, item => item.deptName.includes(searchForm.deptName))
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
  // 重新加载表格以应用展开状态
  const data = tableData.value
  tableData.value = []
  setTimeout(() => { tableData.value = data }, 0)
}

function handleSearch() {
  filterData()
}

function resetSearch() {
  Object.assign(searchForm, { deptName: '', status: '' })
  filterData()
}

function showDialog(row, parentId) {
  isEdit.value = !!row
  if (row) {
    editingDeptId.value = row.deptId
    Object.assign(deptForm, {
      parentId: row.parentId || null,
      deptName: row.deptName,
      leader: row.leader || '',
      phone: row.phone || '',
      sort: row.sort,
      status: row.status
    })
  } else {
    editingDeptId.value = null
    Object.assign(deptForm, { parentId: parentId || null, deptName: '', leader: '', phone: '', sort: 0, status: 0 })
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()

  saving.value = true
  try {
    if (isEdit.value) {
      await updateDept({ deptId: editingDeptId.value, ...deptForm })
      ElMessage.success('部门更新成功')
    } else {
      await addDept(deptForm)
      ElMessage.success('部门创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存部门失败', error)
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除部门"${row.deptName}"吗？`, '警告', { type: 'error' })
    await deleteDeptApi(row.deptId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除部门失败', error)
    }
  }
}

async function handleStatusChange(row) {
  try {
    await updateDept({ deptId: row.deptId, status: row.status })
    ElMessage.success(`部门已${row.status === 0 ? '启用' : '停用'}`)
  } catch (error) {
    row.status = row.status === 0 ? 1 : 0
    console.error('修改状态失败', error)
  }
}
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .total { margin-left: auto; font-size: 13px; color: #909399; } }
</style>
