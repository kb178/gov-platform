<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">证照列表</h1>
      <span class="desc">管理已签发的电子证照，跟踪证照全生命周期</span>
    </div>

    <!-- 统计概览 -->
    <div class="stat-row">
      <div v-for="s in statCards" :key="s.label" class="stat-card" :style="{ borderLeftColor: s.color }">
        <div class="stat-val" :style="{ color: s.color }">{{ s.value }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </div>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="证照编号 / 持证人 / 单位名称" clearable style="width: 260px" />
      <el-select v-model="searchForm.type" placeholder="证照类型" clearable style="width: 150px">
        <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="证照状态" clearable style="width: 130px">
        <el-option label="有效" value="valid" />
        <el-option label="即将过期" value="expiring" />
        <el-option label="已过期" value="expired" />
        <el-option label="已注销" value="revoked" />
      </el-select>
      <el-select v-model="searchForm.collectStatus" placeholder="领取状态" clearable style="width: 130px">
        <el-option label="未领取" value="uncollected" />
        <el-option label="已领取" value="collected" />
        <el-option label="已邮寄" value="mailed" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="handleIssue">➕ 证照签发</el-button>
      <el-button @click="handleBatchPrint">🖨️ 批量打印</el-button>
      <el-button @click="ElMessage.info('批量导出')">📥 批量导出</el-button>
      <span class="total">共 {{ tableData.length }} 张证照</span>
    </div>

    <el-table :data="tableData" @selection-change="handleSelectionChange" :row-class-name="tableRowClass">
      <el-table-column type="selection" width="45" />
      <el-table-column label="证照信息" min-width="200">
        <template #default="{ row }">
          <div class="lic-name">{{ row.name }}</div>
          <div class="lic-code">{{ row.licenseNo }}</div>
        </template>
      </el-table-column>
      <el-table-column label="持证人/单位" width="160">
        <template #default="{ row }">
          <div class="lic-owner">{{ row.owner }}</div>
          <div class="lic-code">{{ row.ownerId }}</div>
        </template>
      </el-table-column>
      <el-table-column label="证照类型" width="120">
        <template #default="{ row }">
          <el-tag size="small" effect="plain">{{ row.typeName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="关联办件" width="130">
        <template #default="{ row }">
          <el-button v-if="row.caseNo" type="primary" link size="small">{{ row.caseNo }}</el-button>
          <span v-else style="color:#C0C4CC; font-size: 12px;">—</span>
        </template>
      </el-table-column>
      <el-table-column label="签发/有效期" width="200">
        <template #default="{ row }">
          <div style="font-size: 13px;">{{ row.issueDate }}</div>
          <div style="font-size: 12px; color: #909399;">至 {{ row.expireDate }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag[row.status]" size="small" effect="dark">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="领取状态" width="100">
        <template #default="{ row }">
          <el-tag :type="collectTag[row.collectStatus]" size="small" effect="plain">{{ collectMap[row.collectStatus] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
          <el-button type="primary" link size="small" @click="handleDownload(row)">下载</el-button>
          <el-dropdown trigger="click" @command="(cmd) => handleMore(cmd, row)" style="margin-left: 6px;">
            <el-button type="primary" link size="small">更多 <el-icon size="12"><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="verify">核验真伪</el-dropdown-item>
                <el-dropdown-item command="print">打印</el-dropdown-item>
                <el-dropdown-item command="mail" :disabled="row.collectStatus === 'mailed'">邮寄</el-dropdown-item>
                <el-dropdown-item command="collect" :disabled="row.collectStatus === 'collected'">确认领取</el-dropdown-item>
                <el-dropdown-item command="reissue">补发</el-dropdown-item>
                <el-dropdown-item command="log">操作日志</el-dropdown-item>
                <el-dropdown-item command="revoke" divided :disabled="row.status === 'revoked'">注销</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <span class="pagination-info">共 {{ tableData.length }} 条记录，每页 10 条</span>
      <el-pagination layout="prev, pager, next, jumper, sizes" :total="tableData.length" :page-size="10" :page-sizes="[10, 20, 50]" />
    </div>

    <!-- 签发弹窗 -->
    <el-dialog v-model="issueVisible" title="证照签发" width="680px" destroy-on-close>
      <el-form :model="issueForm" :rules="issueRules" ref="issueFormRef" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="证照类型" prop="type">
              <el-select v-model="issueForm.type" placeholder="请选择" style="width: 100%">
                <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联办件">
              <el-input v-model="issueForm.caseNo" placeholder="办件单号（选填）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="持证人/单位" prop="owner">
              <el-input v-model="issueForm.owner" placeholder="姓名或单位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="证件号码" prop="ownerId">
              <el-input v-model="issueForm.ownerId" placeholder="身份证号 / 信用代码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="签发日期" prop="issueDate">
              <el-date-picker v-model="issueForm.issueDate" type="date" placeholder="签发日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效期至" prop="expireDate">
              <el-date-picker v-model="issueForm.expireDate" type="date" placeholder="有效期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="领取方式" prop="collectMethod">
              <el-select v-model="issueForm.collectMethod" style="width: 100%">
                <el-option label="窗口领取" value="window" />
                <el-option label="EMS邮寄" value="ems" />
                <el-option label="自助终端" value="self_service" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签发人">
              <el-input v-model="issueForm.issuer" placeholder="签发人姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="issueForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueVisible = false">取消</el-button>
        <el-button type="primary" @click="handleIssueSubmit">确认签发</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="证照详情" width="720px">
      <el-tabs v-model="detailTab">
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="证照名称" :span="2">{{ detail.name }}</el-descriptions-item>
            <el-descriptions-item label="证照编号" :span="2">{{ detail.licenseNo }}</el-descriptions-item>
            <el-descriptions-item label="持证人/单位">{{ detail.owner }}</el-descriptions-item>
            <el-descriptions-item label="证件号码">{{ detail.ownerId }}</el-descriptions-item>
            <el-descriptions-item label="证照类型">{{ detail.typeName }}</el-descriptions-item>
            <el-descriptions-item label="关联办件">{{ detail.caseNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="签发机关">海口市政务服务中心</el-descriptions-item>
            <el-descriptions-item label="签发人">{{ detail.issuer || '系统管理员' }}</el-descriptions-item>
            <el-descriptions-item label="签发日期">{{ detail.issueDate }}</el-descriptions-item>
            <el-descriptions-item label="有效期至">{{ detail.expireDate }}</el-descriptions-item>
            <el-descriptions-item label="证照状态">
              <el-tag :type="statusTag[detail.status]" size="small" effect="dark">{{ statusMap[detail.status] }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="领取状态">
              <el-tag :type="collectTag[detail.collectStatus]" size="small">{{ collectMap[detail.collectStatus] }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="核验码" :span="2">{{ detail.verifyCode }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="操作日志" name="log">
          <el-timeline>
            <el-timeline-item v-for="l in detailLogs" :key="l.time" :timestamp="l.time" placement="top" :type="l.type">
              <div style="font-size: 14px;">{{ l.action }}</div>
              <div style="font-size: 12px; color: #909399; margin-top: 2px;">操作人：{{ l.operator }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 核验弹窗 -->
    <el-dialog v-model="verifyVisible" title="证照核验" width="440px">
      <div class="verify-result">
        <div class="verify-icon success">✓</div>
        <div class="verify-title">核验通过</div>
        <div class="verify-desc">该证照真实有效，信息一致</div>
        <el-descriptions :column="1" border size="small" style="margin-top: 20px;">
          <el-descriptions-item label="证照编号">{{ verifyData.licenseNo }}</el-descriptions-item>
          <el-descriptions-item label="持证人">{{ verifyData.owner }}</el-descriptions-item>
          <el-descriptions-item label="核验码">{{ verifyData.verifyCode }}</el-descriptions-item>
          <el-descriptions-item label="核验时间">{{ new Date().toLocaleString() }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 操作日志弹窗 -->
    <el-dialog v-model="logVisible" title="操作日志" width="560px">
      <el-timeline>
        <el-timeline-item v-for="l in logList" :key="l.time" :timestamp="l.time" placement="top" :type="l.type">
          <div style="font-size: 14px;">{{ l.action }}</div>
          <div style="font-size: 12px; color: #909399; margin-top: 2px;">{{ l.operator }}</div>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

const searchForm = reactive({ keyword: '', type: '', status: '', collectStatus: '' })

const typeOptions = [
  { label: '居民身份证', value: 'id_card' }, { label: '营业执照', value: 'business_license' },
  { label: '不动产权证', value: 'property_cert' }, { label: '食品经营许可证', value: 'food_license' },
  { label: '社保卡', value: 'social_card' }, { label: '医师执业证书', value: 'doctor_license' }
]

const statusMap = { valid: '有效', expiring: '即将过期', expired: '已过期', revoked: '已注销' }
const statusTag = { valid: 'success', expiring: 'warning', expired: 'danger', revoked: 'info' }
const collectMap = { uncollected: '未领取', collected: '已领取', mailed: '已邮寄' }
const collectTag = { uncollected: 'warning', collected: 'success', mailed: 'primary' }

// 统计
const statCards = [
  { label: '证照总量', value: '8,526', color: '#3B82F6' },
  { label: '有效证照', value: '7,832', color: '#10B981' },
  { label: '即将过期', value: '246', color: '#F59E0B' },
  { label: '已过期', value: '312', color: '#EF4444' },
  { label: '已注销', value: '136', color: '#6B7280' }
]

const tableData = ref([
  { id: 1, name: '居民身份证', licenseNo: 'HK-ZJ-20240115-0001', owner: '张三', ownerId: '46010019900115001X', typeName: '居民身份证', caseNo: 'BJ20240115001', issueDate: '2024-01-15', expireDate: '2034-01-15', status: 'valid', collectStatus: 'collected', verifyCode: 'VRF202401150001', issuer: '李科长' },
  { id: 2, name: '营业执照', licenseNo: 'HK-YY-20240112-0001', owner: '海口某某科技有限公司', ownerId: '91460100MA5T12345X', typeName: '营业执照', caseNo: 'BJ20240112002', issueDate: '2024-01-12', expireDate: '2099-12-31', status: 'valid', collectStatus: 'collected', verifyCode: 'VRF202401120002', issuer: '王主任' },
  { id: 3, name: '不动产权证书', licenseNo: 'HK-BD-20240110-0001', owner: '李四', ownerId: '460100198805200034', typeName: '不动产权证', caseNo: 'BJ20240110003', issueDate: '2024-01-10', expireDate: '2099-12-31', status: 'valid', collectStatus: 'mailed', verifyCode: 'VRF202401100003', issuer: '张科长' },
  { id: 4, name: '食品经营许可证', licenseNo: 'HK-SP-20240108-0001', owner: '某某餐饮管理有限公司', ownerId: '91460100MA5T67890A', typeName: '食品经营许可证', caseNo: 'BJ20240108004', issueDate: '2024-01-08', expireDate: '2027-01-07', status: 'valid', collectStatus: 'collected', verifyCode: 'VRF202401080004', issuer: '李科长' },
  { id: 5, name: '社保卡', licenseNo: 'HK-SB-20240105-0001', owner: '王五', ownerId: '460100199203180056', typeName: '社保卡', caseNo: '', issueDate: '2024-01-05', expireDate: '2034-01-05', status: 'valid', collectStatus: 'uncollected', verifyCode: 'VRF202401050005', issuer: '赵主任' },
  { id: 6, name: '营业执照', licenseNo: 'HK-YY-20230615-0002', owner: '海口某某贸易有限公司', ownerId: '91460100MA5T98765B', typeName: '营业执照', caseNo: 'BJ20230615006', issueDate: '2023-06-15', expireDate: '2024-06-14', status: 'expired', collectStatus: 'collected', verifyCode: 'VRF202306150006', issuer: '王主任' },
  { id: 7, name: '食品经营许可证', licenseNo: 'HK-SP-20230301-0002', owner: '某某小吃店', ownerId: '92460100MA5T11111C', typeName: '食品经营许可证', caseNo: 'BJ20230301007', issueDate: '2023-03-01', expireDate: '2026-02-28', status: 'revoked', collectStatus: 'collected', verifyCode: 'VRF202303010007', issuer: '李科长' },
  { id: 8, name: '居民身份证', licenseNo: 'HK-ZJ-20240220-0002', owner: '赵六', ownerId: '460100198512060098', typeName: '居民身份证', caseNo: 'BJ20240220008', issueDate: '2024-02-20', expireDate: '2034-02-20', status: 'valid', collectStatus: 'uncollected', verifyCode: 'VRF202402200008', issuer: '张科长' },
  { id: 9, name: '不动产权证书', licenseNo: 'HK-BD-20230901-0002', owner: '陈七', ownerId: '460100197608150045', typeName: '不动产权证', caseNo: 'BJ20230901009', issueDate: '2023-09-01', expireDate: '2099-12-31', status: 'expiring', collectStatus: 'collected', verifyCode: 'VRF202309010009', issuer: '王主任' },
  { id: 10, name: '医师执业证书', licenseNo: 'HK-YS-20240118-0001', owner: '陈医生', ownerId: '460100198803060023', typeName: '医师执业证书', caseNo: 'BJ20240118010', issueDate: '2024-01-18', expireDate: '2029-01-17', status: 'valid', collectStatus: 'mailed', verifyCode: 'VRF202401180010', issuer: '李科长' }
])

const selectedRows = ref([])
function handleSelectionChange(rows) { selectedRows.value = rows }

function tableRowClass({ row }) {
  if (row.status === 'expiring') return 'row-expiring'
  return ''
}

// 签发弹窗
const issueVisible = ref(false)
const issueFormRef = ref(null)
const issueForm = reactive({ type: '', caseNo: '', owner: '', ownerId: '', issueDate: '', expireDate: '', collectMethod: 'window', issuer: '', remark: '' })
const issueRules = {
  type: [{ required: true, message: '请选择证照类型', trigger: 'change' }],
  owner: [{ required: true, message: '请输入持证人', trigger: 'blur' }],
  ownerId: [{ required: true, message: '请输入证件号码', trigger: 'blur' }],
  issueDate: [{ required: true, message: '请选择签发日期', trigger: 'change' }],
  expireDate: [{ required: true, message: '请选择有效期', trigger: 'change' }],
  collectMethod: [{ required: true, message: '请选择领取方式', trigger: 'change' }]
}

function handleIssue() {
  Object.assign(issueForm, { type: '', caseNo: '', owner: '', ownerId: '', issueDate: '', expireDate: '', collectMethod: 'window', issuer: '', remark: '' })
  issueVisible.value = true
}

function handleIssueSubmit() {
  issueFormRef.value?.validate(valid => {
    if (valid) { ElMessage.success('证照签发成功'); issueVisible.value = false }
  })
}

function handleBatchPrint() {
  if (!selectedRows.value.length) return ElMessage.warning('请先勾选需要打印的证照')
  ElMessage.success(`已发送 ${selectedRows.value.length} 张证照到打印机`)
}

// 详情弹窗
const detailVisible = ref(false)
const detailTab = ref('info')
const detail = reactive({ name: '', licenseNo: '', owner: '', ownerId: '', typeName: '', caseNo: '', issueDate: '', expireDate: '', status: '', collectStatus: '', verifyCode: '', issuer: '' })
const detailLogs = ref([])

function handleView(row) {
  Object.assign(detail, row)
  detailLogs.value = [
    { time: '2024-01-15 14:30:00', action: '证照签发', operator: row.issuer || '管理员', type: 'primary' },
    { time: '2024-01-15 14:32:00', action: '系统生成电子证照文件', operator: '系统', type: '' },
    { time: row.collectStatus === 'mailed' ? '2024-01-16 09:00:00' : '2024-01-15 15:00:00', action: row.collectStatus === 'mailed' ? 'EMS邮寄发出' : row.collectStatus === 'collected' ? '持证人窗口领取' : '等待领取', operator: row.collectStatus === 'mailed' ? '窗口小刘' : row.collectStatus === 'collected' ? '窗口小张' : '系统', type: row.collectStatus === 'uncollected' ? 'warning' : 'success' }
  ]
  detailTab.value = 'info'
  detailVisible.value = true
}

function handleDownload(row) { ElMessage.success(`正在下载：${row.name}（${row.licenseNo}）`) }

// 更多操作
function handleMore(cmd, row) {
  switch (cmd) {
    case 'verify':
      Object.assign(verifyData, row); verifyVisible.value = true; break
    case 'print':
      ElMessage.success(`正在打印：${row.licenseNo}`); break
    case 'mail':
      ElMessageBox.prompt('请输入邮寄地址', 'EMS邮寄', { inputPattern: /.+/, inputErrorMessage: '地址不能为空' }).then(({ value }) => {
        row.collectStatus = 'mailed'; ElMessage.success(`已寄出至：${value}`)
      }).catch(() => {}); break
    case 'collect':
      ElMessageBox.confirm(`确认「${row.owner}」已领取该证照？`, '确认领取').then(() => {
        row.collectStatus = 'collected'; ElMessage.success('已确认领取')
      }).catch(() => {}); break
    case 'reissue':
      ElMessageBox.confirm(`确定要补发「${row.name}」吗？原证照将作废。`, '补发确认', { type: 'warning' }).then(() => {
        ElMessage.success('补发申请已提交')
      }).catch(() => {}); break
    case 'log':
      logList.value = [
        { time: '2024-01-15 14:30:00', action: '证照签发', operator: '李科长', type: 'primary' },
        { time: '2024-01-15 14:32:00', action: '生成电子文件', operator: '系统', type: '' },
        { time: '2024-01-15 15:00:00', action: '窗口领取确认', operator: '窗口小张', type: 'success' }
      ]; logVisible.value = true; break
    case 'revoke':
      ElMessageBox.confirm(`确定要注销「${row.name}」（${row.licenseNo}）吗？注销后不可恢复。`, '注销确认', { type: 'warning' }).then(() => {
        row.status = 'revoked'; ElMessage.success('证照已注销')
      }).catch(() => {}); break
  }
}

// 核验弹窗
const verifyVisible = ref(false)
const verifyData = reactive({ licenseNo: '', owner: '', verifyCode: '' })

// 日志弹窗
const logVisible = ref(false)
const logList = ref([])

function handleSearch() { ElMessage.info('搜索功能待对接接口') }
function resetSearch() { Object.assign(searchForm, { keyword: '', type: '', status: '', collectStatus: '' }) }
</script>

<style lang="scss" scoped>
.stat-row {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin-bottom: 20px;
}
.stat-card {
  background: white; border-radius: 8px; padding: 16px 20px; border-left: 4px solid;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  .stat-val { font-size: 26px; font-weight: 700; }
  .stat-label { font-size: 13px; color: #6B7280; margin-top: 4px; }
}

.toolbar {
  display: flex; align-items: center; gap: 12px; margin-bottom: 16px;
  .total { margin-left: auto; font-size: 13px; color: #909399; }
}
.lic-name { font-weight: 500; font-size: 14px; }
.lic-code { font-size: 12px; color: #909399; margin-top: 2px; }
.lic-owner { font-size: 14px; }
.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }

:deep(.row-expiring) { background: #FFFBEB !important; }

// 核验结果
.verify-result { text-align: center; padding: 10px 0; }
.verify-icon {
  width: 64px; height: 64px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 32px; font-weight: 700; margin: 0 auto 16px;
  &.success { background: #D1FAE5; color: #059669; }
}
.verify-title { font-size: 20px; font-weight: 700; color: #059669; }
.verify-desc { font-size: 14px; color: #6B7280; margin-top: 6px; }
</style>
