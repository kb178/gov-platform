<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">已办事项</h1>
      <span class="desc">查看已处理的审批记录</span>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="stat in statCards" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.bg }">
          <el-icon :size="22"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="事项名称/申请人" clearable style="width: 200px" />
      <el-select v-model="searchForm.result" placeholder="审批结果" clearable style="width: 140px">
        <el-option label="通过" value="approve" />
        <el-option label="驳回" value="reject" />
        <el-option label="退回补正" value="return" />
      </el-select>
      <el-select v-model="searchForm.type" placeholder="事项类型" clearable style="width: 140px">
        <el-option label="户籍办理" value="household" />
        <el-option label="工商登记" value="business" />
        <el-option label="社会保障" value="social" />
        <el-option label="不动产" value="property" />
      </el-select>
      <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 260px" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" stripe>
      <el-table-column label="事项信息" min-width="220">
        <template #default="{ row }">
          <div class="item-info">
            <div class="item-name">{{ row.itemName }}</div>
            <div class="item-no">单号：{{ row.caseNo }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="申请人" width="130">
        <template #default="{ row }">
          <div class="applicant">
            <div class="applicant-avatar">{{ row.applicant.charAt(0) }}</div>
            <span>{{ row.applicant }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="事项类型" width="110" />
      <el-table-column label="审批结果" width="100">
        <template #default="{ row }">
          <el-tag :type="resultTagType(row.result)" size="small" effect="plain">
            {{ row.resultText }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="approveTime" label="审批时间" width="160" sortable />
      <el-table-column prop="duration" label="耗时" width="90" sortable />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
          <el-button type="primary" link size="small" @click="showTimeline(row)">审批记录</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <span class="pagination-info">共 {{ total }} 条记录，每页 10 条</span>
      <el-pagination layout="prev, pager, next" :total="total" :page-size="10" />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="办件详情" width="650px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="办件编号" :span="2">{{ currentRow.caseNo }}</el-descriptions-item>
          <el-descriptions-item label="事项名称" :span="2">{{ currentRow.itemName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentRow.applicant }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentRow.phone }}</el-descriptions-item>
          <el-descriptions-item label="证件号码">{{ currentRow.idCard }}</el-descriptions-item>
          <el-descriptions-item label="事项类型">{{ currentRow.category }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentRow.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="审批时间">{{ currentRow.approveTime }}</el-descriptions-item>
          <el-descriptions-item label="审批结果">
            <el-tag :type="resultTagType(currentRow.result)" size="small">{{ currentRow.resultText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="耗时">{{ currentRow.duration }}</el-descriptions-item>
          <el-descriptions-item label="审批意见" :span="2">{{ currentRow.comment }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section" v-if="currentRow.materials && currentRow.materials.length">
          <h4>申请材料</h4>
          <div class="material-list">
            <div class="material-item" v-for="(m, i) in currentRow.materials" :key="i">
              <el-icon><Document /></el-icon>
              <span>{{ m.name }}</span>
              <el-tag size="small" :type="m.status === '已核验' ? 'success' : 'warning'">{{ m.status }}</el-tag>
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
            <div class="timeline-title">{{ item.title }}</div>
            <div class="timeline-user">{{ item.user }} · {{ item.dept }}</div>
            <div class="timeline-comment" v-if="item.comment">{{ item.comment }}</div>
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, CircleCheck, CircleClose, Warning } from '@element-plus/icons-vue'

const total = ref(126)
const statCards = [
  { label: '本月办理', value: 126, icon: 'Document', bg: '#EFF6FF' },
  { label: '已通过', value: 98, icon: 'CircleCheck', bg: '#F0FDF4' },
  { label: '已驳回', value: 18, icon: 'CircleClose', bg: '#FEF2F2' },
  { label: '退回补正', value: 10, icon: 'Warning', bg: '#FFFBEB' }
]

const searchForm = reactive({ keyword: '', result: '', type: '', dateRange: '' })

const tableData = ref([
  { caseNo: 'BJ20240116001', itemName: '营业执照申请', applicant: '张三', phone: '138****1234', idCard: '460102****1234', category: '工商登记', result: 'approve', resultText: '通过', applyTime: '2024-01-14 09:30', approveTime: '2024-01-16 10:20', duration: '1天1小时', comment: '材料齐全，符合条件，准予办理。', materials: [{ name: '身份证复印件', status: '已核验' }, { name: '经营场所证明', status: '已核验' }, { name: '公司章程', status: '已核验' }] },
  { caseNo: 'BJ20240116002', itemName: '居民身份证换领', applicant: '李四', phone: '139****5678', idCard: '460102****5678', category: '户籍办理', result: 'approve', resultText: '通过', applyTime: '2024-01-15 10:15', approveTime: '2024-01-16 14:30', duration: '1天4小时', comment: '身份信息核实无误，予以换领。', materials: [{ name: '原身份证', status: '已核验' }, { name: '户口簿', status: '已核验' }] },
  { caseNo: 'BJ20240116003', itemName: '食品经营许可证申请', applicant: '王五', phone: '137****9012', idCard: '460102****9012', category: '工商登记', result: 'reject', resultText: '驳回', applyTime: '2024-01-13 11:00', approveTime: '2024-01-16 16:00', duration: '3天5小时', comment: '经营场所不符合食品安全要求，建议整改后重新申请。', materials: [{ name: '身份证复印件', status: '已核验' }, { name: '经营场所平面图', status: '已核验' }, { name: '食品安全管理制度', status: '未通过' }] },
  { caseNo: 'BJ20240116004', itemName: '不动产登记', applicant: '赵六', phone: '136****3456', idCard: '460102****3456', category: '不动产', result: 'approve', resultText: '通过', applyTime: '2024-01-12 14:20', approveTime: '2024-01-16 09:15', duration: '3天19小时', comment: '产权清晰，材料齐全，准予登记。', materials: [{ name: '房产证', status: '已核验' }, { name: '买卖合同', status: '已核验' }, { name: '完税证明', status: '已核验' }] },
  { caseNo: 'BJ20240116005', itemName: '公积金提取申请', applicant: '钱七', phone: '135****7890', idCard: '460102****7890', category: '社会保障', result: 'return', resultText: '退回补正', applyTime: '2024-01-15 15:45', approveTime: '2024-01-16 11:00', duration: '19小时', comment: '购房合同复印件不清晰，请重新提交。', materials: [{ name: '身份证复印件', status: '已核验' }, { name: '购房合同', status: '需补正' }] },
  { caseNo: 'BJ20240116006', itemName: '营业执照变更', applicant: '孙八', phone: '133****2345', idCard: '460102****2345', category: '工商登记', result: 'approve', resultText: '通过', applyTime: '2024-01-14 08:50', approveTime: '2024-01-15 16:20', duration: '1天8小时', comment: '变更事项符合规定，准予变更登记。', materials: [{ name: '变更申请书', status: '已核验' }, { name: '股东会决议', status: '已核验' }] },
  { caseNo: 'BJ20240116007', itemName: '居住证申领', applicant: '周九', phone: '132****6789', idCard: '460102****6789', category: '户籍办理', result: 'reject', resultText: '驳回', applyTime: '2024-01-13 16:30', approveTime: '2024-01-16 10:45', duration: '2天18小时', comment: '居住登记不满半年，暂不符合申领条件。', materials: [{ name: '身份证复印件', status: '已核验' }, { name: '租赁合同', status: '已核验' }] },
  { caseNo: 'BJ20240116008', itemName: '医师执业注册', applicant: '吴十', phone: '131****0123', idCard: '460102****0123', category: '其他', result: 'approve', resultText: '通过', applyTime: '2024-01-10 09:00', approveTime: '2024-01-12 14:30', duration: '2天6小时', comment: '资质审核通过，准予注册。', materials: [{ name: '医师资格证书', status: '已核验' }, { name: '健康证明', status: '已核验' }] }
])

const resultTagType = (result) => {
  const map = { approve: 'success', reject: 'danger', return: 'warning' }
  return map[result] || 'info'
}

// 详情弹窗
const detailVisible = ref(false)
const currentRow = ref(null)
const showDetail = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

// 审批记录弹窗
const timelineVisible = ref(false)
const timelineData = ref([])
const showTimeline = (row) => {
  timelineData.value = [
    { time: row.applyTime, title: '提交申请', user: row.applicant, dept: '群众自助', type: 'primary', comment: '' },
    { time: row.approveTime.replace(/\d{2}:\d{2}$/, '09:00'), title: '窗口受理', user: '陈受理', dept: '综合窗口', type: 'primary', comment: '材料已收取，进入审批流程。' },
    { time: row.approveTime, title: row.resultText, user: '李审批', dept: '审批科', type: row.result === 'approve' ? 'success' : row.result === 'reject' ? 'danger' : 'warning', comment: row.comment }
  ]
  if (row.result === 'return') {
    timelineData.value.push({ time: '待补正', title: '等待补正', user: '-', dept: '-', type: 'info', comment: '请申请人补充材料后重新提交。' })
  }
  timelineVisible.value = true
}

function handleSearch() { ElMessage.info('搜索功能待对接接口') }
function handleReset() {
  searchForm.keyword = ''
  searchForm.result = ''
  searchForm.type = ''
  searchForm.dateRange = ''
}
</script>

<style lang="scss" scoped>
.stat-cards {
  display: flex; gap: 16px; margin-bottom: 20px;
  .stat-card {
    flex: 1; display: flex; align-items: center; gap: 14px;
    background: #fff; border-radius: 10px; padding: 18px 20px;
    box-shadow: 0 1px 4px rgba(0,0,0,0.06);
    .stat-icon {
      width: 44px; height: 44px; border-radius: 10px;
      display: flex; align-items: center; justify-content: center; color: #409EFF;
    }
    .stat-value { font-size: 24px; font-weight: 700; color: #1D2129; }
    .stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
  }
}

.item-info {
  .item-name { font-weight: 500; font-size: 14px; }
  .item-no { font-size: 12px; color: #909399; margin-top: 3px; }
}

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
  h4 { font-size: 14px; color: #303133; margin-bottom: 12px; }
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
