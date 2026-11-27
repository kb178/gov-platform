<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">已办事项</h1>
      <span class="desc">查看已处理的审批记录</span>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="办件编号/申请人" clearable style="width: 200px" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column label="办件编号" width="180">
        <template #default="{ row }">
          <span class="app-no">{{ row.applicationNo }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申请人" width="100">
        <template #default="{ row }">
          <div class="applicant">
            <div class="applicant-avatar">{{ (row.userName || '?').charAt(0) }}</div>
            <span>{{ row.userName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="itemName" label="事项名称" min-width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small" effect="plain">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="160" />
      <el-table-column prop="finishTime" label="办结时间" width="160" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
          <el-button type="primary" link size="small" @click="showTimeline(row)">审批记录</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <span class="pagination-info">共 {{ total }} 条记录</span>
      <el-pagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="办件详情" width="700px">
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="办件编号" :span="2">{{ detailData.applicationNo }}</el-descriptions-item>
          <el-descriptions-item label="事项名称" :span="2">{{ detailData.itemName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detailData.userName }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag size="small">{{ detailData.statusText }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detailData.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="办结时间">{{ detailData.finishTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="加急">{{ detailData.urgency === 1 ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detailData.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 表单数据 -->
        <div v-if="detailFormData" class="detail-section">
          <h4>申请信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item v-for="(val, key) in detailFormData" :key="key" :label="key">{{ val }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 材料清单 -->
        <div v-if="detailMaterials?.length" class="detail-section">
          <h4>申请材料</h4>
          <div class="material-list">
            <div class="material-item" v-for="(m, i) in detailMaterials" :key="i">
              <el-icon><Document /></el-icon>
              <span>{{ m.materialName }}</span>
              <el-tag size="small" :type="m.checkStatus === 1 ? 'success' : m.checkStatus === 2 ? 'danger' : 'info'">
                {{ m.checkStatus === 1 ? '已核验' : m.checkStatus === 2 ? '未通过' : '待核验' }}
              </el-tag>
              <el-button v-if="m.fileUrl" type="primary" link size="small" @click="previewFile(m.fileUrl)">查看</el-button>
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 审批记录弹窗 -->
    <el-dialog v-model="timelineVisible" title="审批记录" width="500px">
      <el-timeline v-if="timelineData.length">
        <el-timeline-item
          v-for="(item, i) in timelineData"
          :key="i"
          :timestamp="item.time"
          :type="item.type"
          placement="top"
        >
          <div class="timeline-content">
            <div class="timeline-title">{{ item.nodeName }} - {{ item.actionText }}</div>
            <div class="timeline-user">{{ item.operatorName }}</div>
            <div class="timeline-comment" v-if="item.opinion">{{ item.opinion }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <template #footer>
        <el-button @click="timelineVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { getDoneList, getApplicationDetail, getApprovalRecords } from '@/api/approval'

// ==================== 列表 ====================
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({ keyword: '' })

async function loadList() {
  loading.value = true
  try {
    const res = await getDoneList({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data?.records || []
    total.value = Number(res.data?.total) || 0
  } catch (e) {
    console.error('加载已办列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadList()
}

function handleReset() {
  searchForm.keyword = ''
  pageNum.value = 1
  loadList()
}

function handlePageChange(page) {
  pageNum.value = page
  loadList()
}

function statusTagType(status) {
  const map = { 5: 'danger', 6: 'success', 3: 'warning' }
  return map[status] || 'info'
}

// ==================== 详情 ====================
const detailVisible = ref(false)
const detailData = ref(null)
const detailFormData = ref(null)
const detailMaterials = ref([])

async function showDetail(row) {
  try {
    const res = await getApplicationDetail(row.applicationId)
    detailData.value = res.data
    detailMaterials.value = res.data?.materials || []

    const formDataStr = res.data?.formData
    if (formDataStr) {
      try {
        const parsed = JSON.parse(formDataStr)
        delete parsed._uploadImages
        detailFormData.value = parsed
      } catch { detailFormData.value = null }
    } else {
      detailFormData.value = null
    }

    detailVisible.value = true
  } catch (e) {
    console.error('获取详情失败', e)
  }
}

function previewFile(url) {
  window.open(url, '_blank')
}

// ==================== 审批记录 ====================
const timelineVisible = ref(false)
const timelineData = ref([])

async function showTimeline(row) {
  try {
    const res = await getApprovalRecords(row.applicationId)
    const records = res.data || []
    timelineData.value = records.map(rec => ({
      ...rec,
      time: rec.createTime,
      actionText: actionText(rec.action),
      type: rec.action === 2 ? 'success' : rec.action === 3 ? 'danger' : rec.action === 4 ? 'warning' : 'primary'
    }))
    timelineVisible.value = true
  } catch (e) {
    console.error('获取审批记录失败', e)
  }
}

function actionText(action) {
  const map = { 1: '受理', 2: '通过', 3: '驳回', 4: '退回补材料', 5: '不予受理' }
  return map[action] || ''
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.app-no { font-family: monospace; font-size: 13px; }

.applicant {
  display: flex; align-items: center; gap: 8px;
  .applicant-avatar {
    width: 28px; height: 28px; border-radius: 50%; background: #DBEAFE;
    display: flex; align-items: center; justify-content: center;
    font-size: 12px; color: #1E40AF; font-weight: 600; flex-shrink: 0;
  }
}

.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }

.detail-section {
  margin-top: 20px;
  h4 { font-size: 14px; color: #303133; margin-bottom: 12px; border-left: 3px solid #409EFF; padding-left: 8px; }
  .material-list { display: flex; flex-direction: column; gap: 8px; }
  .material-item {
    display: flex; align-items: center; gap: 8px; padding: 8px 12px;
    background: #F9FAFB; border-radius: 6px; font-size: 13px;
    .el-icon { color: #909399; }
    span:nth-child(2) { flex: 1; }
  }
}

.timeline-content {
  .timeline-title { font-size: 14px; font-weight: 500; }
  .timeline-user { font-size: 12px; color: #909399; margin-top: 4px; }
  .timeline-comment { font-size: 13px; color: #606266; margin-top: 6px; background: #F9FAFB; padding: 8px 12px; border-radius: 6px; }
}
</style>
