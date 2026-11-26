<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">事项列表</h1>
      <span class="desc">管理政务服务事项</span>
    </div>

    <div class="search-bar">
      <el-select v-model="searchForm.categoryId" placeholder="事项分类" clearable style="width: 160px">
        <el-option v-for="cat in categoryList" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
      </el-select>
      <el-input v-model="searchForm.itemName" placeholder="事项名称" clearable style="width: 200px" />
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="已发布" :value="1" />
        <el-option label="已下线" :value="2" />
        <el-option label="草稿" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showAddDialog">➕ 新增事项</el-button>
      <span class="total">共 {{ total }} 个事项</span>
    </div>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column label="事项信息" min-width="220">
        <template #default="{ row }">
          <div class="item-name">{{ row.itemName }}</div>
          <div class="item-code">事项编码：{{ row.itemCode }}</div>
        </template>
      </el-table-column>
      <el-table-column label="所属分类" width="110">
        <template #default="{ row }">
          <el-tag type="primary" size="small" effect="plain">{{ row.categoryName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deptName" label="主管部门" width="110" show-overflow-tooltip />
      <el-table-column prop="summary" label="描述" min-width="160" show-overflow-tooltip />
      <el-table-column label="在线办理" width="90">
        <template #default="{ row }">
          <el-tag :type="row.supportOnline === 1 ? 'success' : 'info'" size="small">
            {{ row.supportOnline === 1 ? '支持' : '不支持' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type" size="small" effect="dark">
            {{ statusMap[row.status]?.text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="165" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button type="primary" link size="small" @click="handlePublish(row)" v-if="row.status === 0">发布</el-button>
          <el-button type="success" link size="small" @click="handlePublish(row)" v-if="row.status === 2">上线</el-button>
          <el-button type="warning" link size="small" @click="handleOffline(row)" v-if="row.status === 1">下线</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑事项' : '新增事项'" width="700px">
      <el-form :model="itemForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="事项名称" prop="itemName" required>
              <el-input v-model="itemForm.itemName" placeholder="请输入事项名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="事项编码" prop="itemCode" required>
              <el-input v-model="itemForm.itemCode" placeholder="请输入事项编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属分类" prop="categoryId" required>
              <el-tree-select
                v-model="itemForm.categoryId"
                :data="categoryTree"
                :props="{ label: 'categoryName', value: 'categoryId' }"
                placeholder="请选择分类"
                check-strictly
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主管部门" prop="deptId" required>
              <el-tree-select
                v-model="itemForm.deptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'deptId' }"
                placeholder="请选择部门"
                check-strictly
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简短描述" prop="summary">
              <el-input v-model="itemForm.summary" type="textarea" :rows="2" placeholder="请输入简短描述" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="办理条件" prop="applyCondition">
              <div class="rich-text-wrap">
                <div class="format-hint">支持多行输入，每条一行，可使用序号（如：1. xxx）</div>
                <el-input v-model="itemForm.applyCondition" type="textarea" :rows="6" placeholder="请输入办理条件，如：&#10;1. 具有海口市户籍&#10;2. 年满16周岁&#10;3. 持有有效身份证件" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="办理流程" prop="processFlow">
              <div class="rich-text-wrap">
                <div class="format-hint">支持多行输入，每步一行，可使用序号（如：1. xxx）</div>
                <el-input v-model="itemForm.processFlow" type="textarea" :rows="6" placeholder="请输入办理流程，如：&#10;1. 网上提交申请&#10;2. 工作人员审核&#10;3. 现场核验材料&#10;4. 制证发放" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="办理时限" prop="processTime">
              <el-input v-model="itemForm.processTime" placeholder="如：5个工作日" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="办理地点" prop="processLocation">
              <el-input v-model="itemForm.processLocation" placeholder="请输入办理地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收费标准" prop="feeStandard">
              <el-input v-model="itemForm.feeStandard" placeholder="如：免费" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="itemForm.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="在线办理" prop="supportOnline">
              <el-radio-group v-model="itemForm.supportOnline">
                <el-radio :value="1">支持</el-radio>
                <el-radio :value="0">不支持</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序号" prop="sortNum">
              <el-input-number v-model="itemForm.sortNum" :min="0" :max="999" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { getItemList, addItem, updateItem, deleteItem as deleteItemApi, publishItem, offlineItem } from '@/api/item'
import { getCategoryTree } from '@/api/item'
import { getDeptList } from '@/api/system'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({ categoryId: '', itemName: '', status: '' })

const categoryList = ref([])
const categoryTree = ref([])
const deptTree = ref([])

const statusMap = {
  0: { text: '● 草稿', type: 'warning' },
  1: { text: '● 已发布', type: 'success' },
  2: { text: '● 已下线', type: 'info' }
}

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingItemId = ref(null)
const formRef = ref(null)

const itemForm = reactive({
  itemName: '',
  itemCode: '',
  categoryId: '',
  deptId: '',
  summary: '',
  applyCondition: '',
  processFlow: '',
  processTime: '',
  processLocation: '',
  feeStandard: '',
  contactPhone: '',
  supportOnline: 1,
  sortNum: 0
})

const formRules = {
  itemName: [{ required: true, message: '请输入事项名称', trigger: 'blur' }],
  itemCode: [{ required: true, message: '请输入事项编码', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }]
}

onMounted(() => {
  loadData()
  loadCategoryTree()
  loadDeptTree()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      categoryId: searchForm.categoryId || undefined,
      itemName: searchForm.itemName || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    }
    const res = await getItemList(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载事项列表失败', error)
  } finally {
    loading.value = false
  }
}

async function loadCategoryTree() {
  try {
    const res = await getCategoryTree()
    categoryTree.value = res.data || []
    // 扁平化用于搜索筛选
    const flatten = (list) => {
      let result = []
      list.forEach(item => {
        result.push(item)
        if (item.children) result = result.concat(flatten(item.children))
      })
      return result
    }
    categoryList.value = flatten(res.data || [])
  } catch (error) {
    console.error('加载分类列表失败', error)
  }
}

async function loadDeptTree() {
  try {
    const res = await getDeptList()
    deptTree.value = res.data || []
  } catch (error) {
    console.error('加载部门列表失败', error)
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
  Object.assign(searchForm, { categoryId: '', itemName: '', status: '' })
  handleSearch()
}

function showAddDialog() {
  isEdit.value = false
  editingItemId.value = null
  Object.assign(itemForm, { itemName: '', itemCode: '', categoryId: '', deptId: '', summary: '', applyCondition: '', processFlow: '', processTime: '', processLocation: '', feeStandard: '', contactPhone: '', supportOnline: 1, sortNum: 0 })
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  editingItemId.value = row.itemId
  Object.assign(itemForm, {
    itemName: row.itemName,
    itemCode: row.itemCode,
    categoryId: row.categoryId,
    deptId: row.deptId,
    summary: row.summary || '',
    applyCondition: row.applyCondition || '',
    processFlow: row.processFlow || '',
    processTime: row.processTime || '',
    processLocation: row.processLocation || '',
    feeStandard: row.feeStandard || '',
    contactPhone: row.contactPhone || '',
    supportOnline: row.supportOnline != null ? row.supportOnline : 1,
    sortNum: row.sortNum || 0
  })
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()

  saving.value = true
  try {
    if (isEdit.value) {
      await updateItem({ itemId: editingItemId.value, ...itemForm })
      ElMessage.success('事项更新成功')
    } else {
      await addItem(itemForm)
      ElMessage.success('事项创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存事项失败', error)
  } finally {
    saving.value = false
  }
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm(`确定要发布事项"${row.itemName}"吗？`, '提示', { type: 'warning' })
    await publishItem(row.itemId)
    ElMessage.success('事项已发布')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布事项失败', error)
    }
  }
}

async function handleOffline(row) {
  try {
    await ElMessageBox.confirm(`确定要下线事项"${row.itemName}"吗？`, '提示', { type: 'warning' })
    await offlineItem(row.itemId)
    ElMessage.success('事项已下线')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下线事项失败', error)
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除事项"${row.itemName}"吗？删除后不可恢复。`, '警告', { type: 'error' })
    await deleteItemApi(row.itemId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除事项失败', error)
    }
  }
}
</script>

<style lang="scss" scoped>
.toolbar {
  display: flex; align-items: center; gap: 12px; margin-bottom: 16px;
  .total { margin-left: auto; font-size: 13px; color: #909399; }
}
.item-name { font-weight: 500; font-size: 14px; }
.item-code { font-size: 12px; color: #909399; margin-top: 4px; }
.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }
.rich-text-wrap { width: 100%; }
.format-hint { font-size: 12px; color: #9CA3AF; margin-bottom: 4px; }
</style>
