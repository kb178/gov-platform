<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">菜单管理</h1>
      <span class="desc">管理系统菜单和权限标识</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.menuName" placeholder="菜单名称" clearable style="width: 180px" />
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="0" />
        <el-option label="停用" :value="1" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">➕ 新增菜单</el-button>
      <el-button @click="toggleExpand">{{ isExpand ? '折叠全部' : '展开全部' }}</el-button>
    </div>

    <el-table :data="tableData" row-key="menuId" :default-expand-all="isExpand" :tree-props="{ children: 'children' }" v-loading="loading">
      <el-table-column prop="menuName" label="菜单名称" min-width="200" />
      <el-table-column prop="icon" label="图标" width="80">
        <template #default="{ row }">
          <span>{{ row.icon }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column prop="perms" label="权限标识" width="160" show-overflow-tooltip />
      <el-table-column prop="path" label="路由路径" width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="typeMap[row.menuType].type" size="small">{{ typeMap[row.menuType].text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="0" :inactive-value="1" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
          <el-button type="primary" link size="small" @click="showDialog(null, row.menuId)">新增</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="600px">
      <el-form :model="menuForm" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="menuForm.parentId"
            :data="tableData"
            :props="{ label: 'menuName', value: 'menuId', children: 'children' }"
            placeholder="选择上级菜单（不选则为顶级）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType" required>
          <el-radio-group v-model="menuForm.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="menuName" required>
              <el-input v-model="menuForm.menuName" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="menuForm.sort" :min="0" :max="999" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="menuForm.menuType !== 'F'">
          <el-col :span="12">
            <el-form-item label="图标" prop="icon">
              <el-input v-model="menuForm.icon" placeholder="请输入图标名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路由路径" prop="path">
              <el-input v-model="menuForm.path" placeholder="请输入路由路径" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="menuForm.menuType !== 'M'" label="权限标识" prop="perms">
          <el-input v-model="menuForm.perms" placeholder="如：system:user:list" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="menuForm.status">
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuList, addMenu, updateMenu, deleteMenu as deleteMenuApi } from '@/api/system'

const loading = ref(false)
const saving = ref(false)
const searchForm = reactive({ menuName: '', status: '' })
const isExpand = ref(true)
const tableData = ref([])
const allData = ref([])

const typeMap = {
  M: { text: '目录', type: 'primary' },
  C: { text: '菜单', type: 'success' },
  F: { text: '按钮', type: 'warning' }
}

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingMenuId = ref(null)
const formRef = ref(null)

const menuForm = reactive({
  parentId: null,
  menuType: 'C',
  menuName: '',
  icon: '',
  sort: 0,
  perms: '',
  path: '',
  status: 0
})

const formRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMenuList()
    allData.value = res.data || []
    filterData()
  } catch (error) {
    console.error('加载菜单列表失败', error)
  } finally {
    loading.value = false
  }
}

function filterData() {
  let list = [...allData.value]
  if (searchForm.menuName) {
    list = filterTree(list, item => item.menuName.includes(searchForm.menuName))
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
  Object.assign(searchForm, { menuName: '', status: '' })
  filterData()
}

function showDialog(row, parentId) {
  isEdit.value = !!row
  if (row) {
    editingMenuId.value = row.menuId
    Object.assign(menuForm, {
      parentId: row.parentId || null,
      menuType: row.menuType,
      menuName: row.menuName,
      icon: row.icon || '',
      sort: row.sort,
      perms: row.perms || '',
      path: row.path || '',
      status: row.status
    })
  } else {
    editingMenuId.value = null
    Object.assign(menuForm, { parentId: parentId || null, menuType: 'C', menuName: '', icon: '', sort: 0, perms: '', path: '', status: 0 })
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()

  saving.value = true
  try {
    if (isEdit.value) {
      await updateMenu({ menuId: editingMenuId.value, ...menuForm })
      ElMessage.success('菜单更新成功')
    } else {
      await addMenu(menuForm)
      ElMessage.success('菜单创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存菜单失败', error)
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除菜单"${row.menuName}"吗？`, '警告', { type: 'error' })
    await deleteMenuApi(row.menuId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除菜单失败', error)
    }
  }
}

async function handleStatusChange(row) {
  try {
    await updateMenu({ menuId: row.menuId, status: row.status })
    ElMessage.success(`菜单已${row.status === 0 ? '启用' : '停用'}`)
  } catch (error) {
    row.status = row.status === 0 ? 1 : 0
    console.error('修改状态失败', error)
  }
}
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
</style>
