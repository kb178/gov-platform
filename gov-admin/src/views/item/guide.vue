<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">办事指南</h1>
      <span class="desc">查看事项办理指南</span>
    </div>

    <div class="search-bar">
      <el-select v-model="searchForm.categoryId" placeholder="事项分类" clearable style="width: 160px">
        <el-option v-for="cat in categoryList" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
      </el-select>
      <el-input v-model="searchForm.itemName" placeholder="事项名称" clearable style="width: 200px" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column label="事项信息" min-width="240">
        <template #default="{ row }">
          <div class="item-name">{{ row.itemName }}</div>
          <div class="item-code">事项编码：{{ row.itemCode }}</div>
        </template>
      </el-table-column>
      <el-table-column label="所属分类" width="120">
        <template #default="{ row }">
          <el-tag type="primary" size="small" effect="plain">{{ row.categoryName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deptName" label="主管部门" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '已发布' : '未发布' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showGuide(row)">查看指南</el-button>
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

    <!-- 办事指南详情弹窗 -->
    <el-dialog v-model="guideVisible" :title="currentItem?.itemName + ' - 办事指南'" width="800px">
      <div class="guide-content" v-if="currentItem">
        <div class="guide-section">
          <h3>基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="事项名称">{{ currentItem.itemName }}</el-descriptions-item>
            <el-descriptions-item label="事项编码">{{ currentItem.itemCode }}</el-descriptions-item>
            <el-descriptions-item label="所属分类">{{ currentItem.categoryName }}</el-descriptions-item>
            <el-descriptions-item label="主管部门">{{ currentItem.deptName }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="guide-section" v-if="currentItem.description">
          <h3>事项描述</h3>
          <p>{{ currentItem.description }}</p>
        </div>

        <div class="guide-section" v-if="currentItem.condition">
          <h3>办理条件</h3>
          <p>{{ currentItem.condition }}</p>
        </div>

        <div class="guide-section">
          <h3>办理流程</h3>
          <el-steps :active="4" align-center>
            <el-step title="申请" description="提交申请材料" />
            <el-step title="受理" description="窗口受理审核" />
            <el-step title="审批" description="部门审批" />
            <el-step title="办结" description="领取结果" />
          </el-steps>
        </div>

        <div class="guide-section">
          <h3>所需材料</h3>
          <el-table :data="materials" border size="small">
            <el-table-column prop="materialName" label="材料名称" min-width="150" />
            <el-table-column prop="materialType" label="材料类型" width="100" />
            <el-table-column prop="quantity" label="份数" width="80" />
            <el-table-column prop="remark" label="备注" min-width="150" />
          </el-table>
        </div>

        <div class="guide-section" v-if="currentItem.timeLimit">
          <h3>办理时限</h3>
          <p>{{ currentItem.timeLimit }}</p>
        </div>

        <div class="guide-section" v-if="currentItem.location">
          <h3>办理地点</h3>
          <p>{{ currentItem.location }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="guideVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getItemList, getCategoryTree, getMaterialsByItem } from '@/api/item'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({ categoryId: '', itemName: '' })
const categoryList = ref([])

const guideVisible = ref(false)
const currentItem = ref(null)
const materials = ref([])

onMounted(() => {
  loadData()
  loadCategoryList()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      categoryId: searchForm.categoryId || undefined,
      itemName: searchForm.itemName || undefined,
      status: 1 // 只查询已发布的事项
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

async function loadCategoryList() {
  try {
    const res = await getCategoryTree()
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

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function resetSearch() {
  Object.assign(searchForm, { categoryId: '', itemName: '' })
  handleSearch()
}

async function showGuide(row) {
  currentItem.value = row
  guideVisible.value = true

  // 加载事项的材料列表
  try {
    const res = await getMaterialsByItem(row.itemId)
    materials.value = res.data || []
  } catch (error) {
    console.error('加载材料列表失败', error)
    materials.value = []
  }
}
</script>

<style lang="scss" scoped>
.item-name { font-weight: 500; font-size: 14px; }
.item-code { font-size: 12px; color: #909399; margin-top: 4px; }
.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }

.guide-content {
  .guide-section {
    margin-bottom: 24px;
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #1F2937;
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 2px solid #E5E7EB;
    }
    p {
      font-size: 14px;
      color: #4B5563;
      line-height: 1.8;
    }
  }
}
</style>
