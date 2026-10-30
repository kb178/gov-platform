<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">证照模板</h1>
      <span class="desc">管理电子证照模板，配置字段映射与打印样式</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.name" placeholder="模板名称" clearable style="width: 200px" />
      <el-select v-model="searchForm.category" placeholder="业务分类" clearable style="width: 150px">
        <el-option label="个人证照" value="personal" />
        <el-option label="企业证照" value="enterprise" />
        <el-option label="经营许可" value="license" />
        <el-option label="权属证书" value="ownership" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 130px">
        <el-option label="已启用" value="active" />
        <el-option label="已停用" value="inactive" />
        <el-option label="草稿" value="draft" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">➕ 新增模板</el-button>
      <el-radio-group v-model="viewMode" size="small" style="margin-left: auto;">
        <el-radio-button value="card">卡片</el-radio-button>
        <el-radio-button value="table">列表</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 卡片视图 -->
    <div v-if="viewMode === 'card'" class="card-grid">
      <div v-for="row in tableData" :key="row.id" class="tpl-card" :class="{ inactive: row.status === 'inactive' }">
        <div class="tpl-card-preview" @click="handlePreview(row)">
          <div class="preview-mini">
            <div class="mini-header" :style="{ background: row.headerColor }">{{ row.typeName }}</div>
            <div class="mini-body">
              <div v-for="f in row.previewFields.slice(0, 3)" :key="f.label" class="mini-field">
                <span class="mf-label">{{ f.label }}</span>
                <span class="mf-value">{{ f.value }}</span>
              </div>
            </div>
            <div class="mini-footer">海口市政务服务中心</div>
          </div>
          <div class="preview-hover">
            <el-icon :size="28"><ZoomIn /></el-icon>
            <span>点击预览</span>
          </div>
        </div>
        <div class="tpl-card-info">
          <div class="tpl-card-name">{{ row.name }}</div>
          <div class="tpl-card-meta">
            <span>{{ row.code }}</span>
            <el-tag size="small" :type="categoryTag[row.category]" effect="plain">{{ categoryMap[row.category] }}</el-tag>
          </div>
          <div class="tpl-card-bottom">
            <div class="tpl-card-tags">
              <el-tag size="small" type="info" effect="plain">{{ row.paperSize }}</el-tag>
              <el-tag size="small" type="info" effect="plain">{{ row.orientation === 'portrait' ? '纵向' : '横向' }}</el-tag>
              <el-tag size="small" v-if="row.hasQrcode" type="success" effect="plain">含二维码</el-tag>
              <el-tag size="small" v-if="row.hasSeal" type="warning" effect="plain">含印章</el-tag>
            </div>
            <div class="tpl-card-actions">
              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, row)">
                <el-button type="primary" link size="small">操作 <el-icon><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="fields">字段配置</el-dropdown-item>
                    <el-dropdown-item command="copy">复制</el-dropdown-item>
                    <el-dropdown-item command="history">版本历史</el-dropdown-item>
                    <el-dropdown-item command="status">{{ row.status === 'active' ? '停用' : '启用' }}</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
      <!-- 新增卡片 -->
      <div class="tpl-card add-card" @click="handleAdd">
        <div class="add-inner">
          <el-icon :size="40"><Plus /></el-icon>
          <span>新增模板</span>
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <el-table v-else :data="tableData">
      <el-table-column label="模板信息" min-width="220">
        <template #default="{ row }">
          <div class="tpl-name">{{ row.name }}</div>
          <div class="tpl-code">{{ row.code }}</div>
        </template>
      </el-table-column>
      <el-table-column label="业务分类" width="110">
        <template #default="{ row }">
          <el-tag :type="categoryTag[row.category]" size="small" effect="plain">{{ categoryMap[row.category] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="纸张/方向" width="120">
        <template #default="{ row }">{{ row.paperSize }} / {{ row.orientation === 'portrait' ? '纵向' : '横向' }}</template>
      </el-table-column>
      <el-table-column label="特性" width="160">
        <template #default="{ row }">
          <el-tag v-if="row.hasQrcode" size="small" type="success" effect="plain" style="margin-right:4px">二维码</el-tag>
          <el-tag v-if="row.hasSeal" size="small" type="warning" effect="plain" style="margin-right:4px">印章</el-tag>
          <el-tag v-if="row.hasWatermark" size="small" type="info" effect="plain">水印</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="80" />
      <el-table-column prop="usageCount" label="使用次数" width="90" />
      <el-table-column prop="updateTime" label="更新时间" width="110" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTag[row.status]" size="small" effect="dark">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handlePreview(row)">预览</el-button>
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="primary" link size="small" @click="handleFields(row)">字段</el-button>
          <el-button type="primary" link size="small" @click="handleCopy(row)">复制</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <span class="pagination-info">共 {{ tableData.length }} 个模板</span>
      <el-pagination layout="prev, pager, next, jumper" :total="tableData.length" :page-size="12" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模板' : '新增模板'" width="720px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="模板名称" prop="name">
              <el-input v-model="form.name" placeholder="如：居民身份证模板" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板编码" prop="code">
              <el-input v-model="form.code" placeholder="自动生成或手动输入" :disabled="isEdit" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="业务分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择" style="width: 100%">
                <el-option label="个人证照" value="personal" />
                <el-option label="企业证照" value="enterprise" />
                <el-option label="经营许可" value="license" />
                <el-option label="权属证书" value="ownership" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联事项">
              <el-select v-model="form.itemIds" multiple placeholder="关联办事事项" style="width: 100%">
                <el-option label="居民身份证办理" value="1" />
                <el-option label="营业执照办理" value="2" />
                <el-option label="不动产登记" value="3" />
                <el-option label="食品经营许可" value="4" />
                <el-option label="社保卡申领" value="5" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="纸张大小" prop="paperSize">
              <el-select v-model="form.paperSize" style="width: 100%">
                <el-option label="A4" value="A4" />
                <el-option label="A5" value="A5" />
                <el-option label="B5" value="B5" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="方向">
              <el-select v-model="form.orientation" style="width: 100%">
                <el-option label="纵向" value="portrait" />
                <el-option label="横向" value="landscape" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="版本号" prop="version">
              <el-input v-model="form.version" placeholder="v1.0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="含二维码">
              <el-switch v-model="form.hasQrcode" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="含印章">
              <el-switch v-model="form.hasSeal" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="含水印">
              <el-switch v-model="form.hasWatermark" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="模板说明">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="模板用途说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字段配置弹窗 -->
    <el-dialog v-model="fieldsVisible" title="字段配置" width="700px">
      <div class="fields-toolbar">
        <span style="font-size: 14px; color: #606266;">配置证照模板中显示的字段及顺序</span>
        <el-button type="primary" size="small" @click="ElMessage.info('新增字段')">添加字段</el-button>
      </div>
      <el-table :data="fieldsList" border size="small">
        <el-table-column prop="name" label="字段名称" width="140" />
        <el-table-column prop="key" label="字段标识" width="150" />
        <el-table-column label="是否显示" width="90">
          <template #default="{ row }">
            <el-switch v-model="row.visible" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="fontSize" label="字号" width="80" />
        <el-table-column prop="position" label="位置" width="100" />
        <el-table-column label="操作" width="100">
          <template #default="{ row, $index }">
            <el-button type="primary" link size="small" @click="ElMessage.info('编辑字段')">编辑</el-button>
            <el-button type="danger" link size="small" @click="fieldsList.splice($index, 1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="fieldsVisible = false">取消</el-button>
        <el-button type="primary" @click="ElMessage.success('字段配置已保存'); fieldsVisible = false">保存</el-button>
      </template>
    </el-dialog>

    <!-- 版本历史弹窗 -->
    <el-dialog v-model="historyVisible" title="版本历史" width="560px">
      <el-timeline>
        <el-timeline-item v-for="h in historyList" :key="h.version" :timestamp="h.time" placement="top"
          :type="h.current ? 'primary' : ''">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-tag :type="h.current ? 'primary' : 'info'" size="small">{{ h.version }}</el-tag>
            <span style="font-size: 14px;">{{ h.desc }}</span>
          </div>
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">操作人：{{ h.operator }}</div>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="模板预览" width="520px">
      <div class="preview-cert" :class="previewData.orientation">
        <div class="cert-header" :style="{ background: previewData.headerColor }">
          <div class="cert-title">{{ previewData.typeName }}</div>
          <div class="cert-subtitle">PEOPLE'S REPUBLIC OF CHINA</div>
        </div>
        <div class="cert-body">
          <div v-for="f in previewData.fields" :key="f.label" class="cert-field">
            <span class="cf-label">{{ f.label }}</span>
            <span class="cf-value" :class="{ 'cf-long': f.value && f.value.length > 16 }">{{ f.value }}</span>
          </div>
        </div>
        <div class="cert-footer">
          <div class="cert-seal" v-if="previewData.hasSeal">
            <div class="seal-circle">海口市<br>政务服务中心</div>
          </div>
          <div class="cert-qrcode" v-if="previewData.hasQrcode">
            <div class="qr-placeholder">QR</div>
          </div>
          <div class="cert-issuer">
            <div>签发机关：海口市政务服务中心</div>
            <div>签发日期：2024年01月15日</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ZoomIn, Plus, ArrowDown } from '@element-plus/icons-vue'

const searchForm = reactive({ name: '', category: '', status: '' })
const viewMode = ref('card')

const categoryMap = { personal: '个人证照', enterprise: '企业证照', license: '经营许可', ownership: '权属证书' }
const categoryTag = { personal: '', enterprise: 'success', license: 'warning', ownership: 'danger' }
const statusMap = { active: '已启用', inactive: '已停用', draft: '草稿' }
const statusTag = { active: 'success', inactive: 'info', draft: 'warning' }

const tableData = ref([
  {
    id: 1, name: '居民身份证模板', code: 'TPL-ID-001', type: 'id_card', typeName: '居民身份证',
    category: 'personal', paperSize: 'A4', orientation: 'portrait', version: 'v2.1',
    hasQrcode: true, hasSeal: false, hasWatermark: true, usageCount: 8234,
    updateTime: '2024-01-15', status: 'active', headerColor: '#1E40AF',
    previewFields: [
      { label: '姓名', value: '张三' }, { label: '性别', value: '男' }, { label: '民族', value: '汉' },
      { label: '出生日期', value: '1990年01月15日' }, { label: '住址', value: '海南省海口市龙华区滨海大道1号' },
      { label: '公民身份号码', value: '46010019900115001X' }
    ]
  },
  {
    id: 2, name: '营业执照模板', code: 'TPL-BL-001', type: 'business_license', typeName: '营业执照',
    category: 'enterprise', paperSize: 'A3', orientation: 'landscape', version: 'v1.5',
    hasQrcode: true, hasSeal: true, hasWatermark: false, usageCount: 5621,
    updateTime: '2024-01-12', status: 'active', headerColor: '#DC2626',
    previewFields: [
      { label: '统一社会信用代码', value: '91460100MA5T12345X' }, { label: '名称', value: '海口某某科技有限公司' },
      { label: '类型', value: '有限责任公司' }, { label: '法定代表人', value: '李四' },
      { label: '注册资本', value: '500万元整' }, { label: '成立日期', value: '2020年06月18日' },
      { label: '营业期限', value: '2020年06月18日至 长期' }, { label: '经营范围', value: '软件开发、信息技术咨询...' }
    ]
  },
  {
    id: 3, name: '不动产权证书模板', code: 'TPL-PC-001', type: 'property_cert', typeName: '不动产权证书',
    category: 'ownership', paperSize: 'A4', orientation: 'portrait', version: 'v1.3',
    hasQrcode: true, hasSeal: true, hasWatermark: true, usageCount: 3245,
    updateTime: '2024-01-10', status: 'active', headerColor: '#7C3AED',
    previewFields: [
      { label: '权利人', value: '王五' }, { label: '坐落', value: '海口市美兰区海甸岛五西路88号' },
      { label: '不动产单元号', value: '460100001001GB00001F00010001' },
      { label: '权利类型', value: '国有建设用地使用权/房屋所有权' }, { label: '面积', value: '128.56㎡' }
    ]
  },
  {
    id: 4, name: '食品经营许可证模板', code: 'TPL-FL-001', type: 'food_license', typeName: '食品经营许可证',
    category: 'license', paperSize: 'A4', orientation: 'portrait', version: 'v1.2',
    hasQrcode: true, hasSeal: true, hasWatermark: false, usageCount: 1856,
    updateTime: '2024-01-08', status: 'active', headerColor: '#059669',
    previewFields: [
      { label: '经营者名称', value: '海口某某餐饮管理有限公司' }, { label: '社会信用代码', value: '91460100MA5T67890A' },
      { label: '经营场所', value: '海口市龙华区国贸路36号' }, { label: '法定代表人', value: '赵六' },
      { label: '经营项目', value: '热食类食品制售；自制饮品制售' }, { label: '有效期至', value: '2027年01月07日' }
    ]
  },
  {
    id: 5, name: '社保卡模板', code: 'TPL-SC-001', type: 'social_card', typeName: '社会保障卡',
    category: 'personal', paperSize: 'A4', orientation: 'portrait', version: 'v1.0',
    hasQrcode: false, hasSeal: false, hasWatermark: true, usageCount: 987,
    updateTime: '2024-01-05', status: 'inactive', headerColor: '#0891B2',
    previewFields: [
      { label: '姓名', value: '刘七' }, { label: '社会保障号码', value: '460100199508120078' },
      { label: '卡号', value: '6228000000000001234' }, { label: '发卡日期', value: '2024年01月05日' }
    ]
  },
  {
    id: 6, name: '医师执业证书模板', code: 'TPL-DR-001', type: 'doctor_license', typeName: '医师执业证书',
    category: 'license', paperSize: 'A4', orientation: 'portrait', version: 'v1.0',
    hasQrcode: true, hasSeal: true, hasWatermark: true, usageCount: 432,
    updateTime: '2024-01-03', status: 'draft', headerColor: '#BE185D',
    previewFields: [
      { label: '姓名', value: '陈医生' }, { label: '性别', value: '男' },
      { label: '医师资格证书编码', value: '2015460000001' }, { label: '执业地点', value: '海口市人民医院' },
      { label: '执业类别', value: '临床' }, { label: '执业范围', value: '内科专业' }
    ]
  }
])

// 弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  name: '', code: '', category: '', itemIds: [], paperSize: 'A4',
  orientation: 'portrait', version: '', hasQrcode: true, hasSeal: false, hasWatermark: false, remark: ''
})
const rules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
  category: [{ required: true, message: '请选择业务分类', trigger: 'change' }],
  paperSize: [{ required: true, message: '请选择纸张大小', trigger: 'change' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }]
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { name: '', code: '', category: '', itemIds: [], paperSize: 'A4', orientation: 'portrait', version: 'v1.0', hasQrcode: true, hasSeal: false, hasWatermark: false, remark: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, { name: row.name, code: row.code, category: row.category, itemIds: [], paperSize: row.paperSize, orientation: row.orientation, version: row.version, hasQrcode: row.hasQrcode, hasSeal: row.hasSeal, hasWatermark: row.hasWatermark, remark: '' })
  dialogVisible.value = true
}

function handleSubmit() {
  formRef.value?.validate(valid => {
    if (valid) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false }
  })
}

function handleCopy(row) { ElMessage.success(`已复制模板：${row.name}`) }

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除模板「${row.name}」吗？`, '删除确认', { type: 'warning' }).then(() => {
    tableData.value = tableData.value.filter(d => d.id !== row.id); ElMessage.success('删除成功')
  }).catch(() => {})
}

function handleCommand(cmd, row) {
  const actions = { edit: handleEdit, fields: handleFields, copy: handleCopy, history: handleHistory, delete: handleDelete }
  if (cmd === 'status') {
    row.status = row.status === 'active' ? 'inactive' : 'active'
    ElMessage.success(row.status === 'active' ? '已启用' : '已停用')
  } else if (actions[cmd]) {
    actions[cmd](row)
  }
}

// 字段配置
const fieldsVisible = ref(false)
const fieldsList = ref([])
function handleFields(row) {
  fieldsList.value = row.previewFields.map((f, i) => ({
    name: f.label, key: f.label.toLowerCase().replace(/\s/g, '_'), visible: true, fontSize: '14px', position: i < 3 ? '左侧' : '右侧'
  }))
  fieldsVisible.value = true
}

// 版本历史
const historyVisible = ref(false)
const historyList = ref([])
function handleHistory(row) {
  historyList.value = [
    { version: row.version, desc: '调整字段布局', time: row.updateTime, operator: '管理员', current: true },
    { version: 'v1.0', desc: '初始版本', time: '2023-06-15', operator: '管理员', current: false }
  ]
  historyVisible.value = true
}

// 预览
const previewVisible = ref(false)
const previewData = reactive({ typeName: '', fields: [], headerColor: '', orientation: '', hasSeal: false, hasQrcode: false })
function handlePreview(row) {
  Object.assign(previewData, { typeName: row.typeName, fields: row.previewFields, headerColor: row.headerColor, orientation: row.orientation, hasSeal: row.hasSeal, hasQrcode: row.hasQrcode })
  previewVisible.value = true
}

function handleSearch() { ElMessage.info('搜索功能待对接接口') }
function resetSearch() { Object.assign(searchForm, { name: '', category: '', status: '' }) }
</script>

<style lang="scss" scoped>
.toolbar {
  display: flex; align-items: center; gap: 12px; margin-bottom: 16px;
}

// ===== 卡片网格 =====
.card-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px;
}
.tpl-card {
  background: white; border-radius: 12px; border: 1px solid #E5E7EB; overflow: hidden;
  transition: all 0.2s;
  &:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); transform: translateY(-2px); }
  &.inactive { opacity: 0.65; }
}
.tpl-card-preview {
  position: relative; cursor: pointer; padding: 16px; background: #F9FAFB;
}
.preview-hover {
  position: absolute; inset: 0; background: rgba(0,0,0,0.4); display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 6px; color: white; font-size: 13px; opacity: 0;
  transition: opacity 0.2s;
  &:hover { opacity: 1; }
}
.preview-mini {
  background: white; border: 1px solid #D1D5DB; border-radius: 6px; overflow: hidden; font-size: 11px;
  .mini-header { color: white; text-align: center; padding: 8px; font-size: 13px; font-weight: 600; letter-spacing: 2px; }
  .mini-body { padding: 10px 12px; }
  .mini-field { display: flex; padding: 3px 0; border-bottom: 1px dashed #F3F4F6; }
  .mf-label { width: 70px; color: #9CA3AF; flex-shrink: 0; }
  .mf-value { color: #374151; }
  .mini-footer { padding: 6px 12px; background: #F3F4F6; font-size: 10px; color: #9CA3AF; text-align: center; }
}
.tpl-card-info { padding: 14px 16px; }
.tpl-card-name { font-size: 15px; font-weight: 600; color: #1F2937; margin-bottom: 6px; }
.tpl-card-meta { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #9CA3AF; margin-bottom: 10px; }
.tpl-card-bottom { display: flex; align-items: center; justify-content: space-between; }
.tpl-card-tags { display: flex; gap: 4px; flex-wrap: wrap; }

.add-card {
  display: flex; align-items: center; justify-content: center; min-height: 320px;
  border: 2px dashed #D1D5DB; cursor: pointer; background: #FAFAFA;
  &:hover { border-color: #3B82F6; background: #EFF6FF; }
  .add-inner { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #9CA3AF; font-size: 14px; }
}

// ===== 字段配置 =====
.fields-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }

// ===== 预览证书 =====
.preview-cert {
  background: white; border: 2px solid #E5E7EB; border-radius: 8px; overflow: hidden; max-width: 460px; margin: 0 auto;
  .cert-header {
    text-align: center; padding: 20px; color: white;
    .cert-title { font-size: 22px; font-weight: 700; letter-spacing: 6px; }
    .cert-subtitle { font-size: 10px; margin-top: 4px; opacity: 0.8; letter-spacing: 1px; }
  }
  .cert-body { padding: 20px 24px; }
  .cert-field {
    display: flex; padding: 8px 0; border-bottom: 1px dashed #E5E7EB;
    .cf-label { width: 130px; color: #6B7280; font-size: 13px; flex-shrink: 0; }
    .cf-value { color: #1F2937; font-size: 14px; &.cf-long { font-size: 12px; } }
  }
  .cert-footer {
    padding: 16px 24px; display: flex; align-items: flex-end; justify-content: space-between; position: relative; min-height: 80px;
  }
  .cert-issuer { font-size: 12px; color: #6B7280; line-height: 1.8; }
  .cert-seal {
    position: absolute; right: 100px; bottom: 10px;
    .seal-circle {
      width: 72px; height: 72px; border: 3px solid #DC2626; border-radius: 50%; display: flex;
      align-items: center; justify-content: center; text-align: center; color: #DC2626;
      font-size: 11px; font-weight: 600; line-height: 1.4; transform: rotate(-15deg);
    }
  }
  .cert-qrcode {
    position: absolute; right: 24px; bottom: 16px;
    .qr-placeholder { width: 48px; height: 48px; border: 1px solid #D1D5DB; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 11px; color: #9CA3AF; background: #F9FAFB; }
  }
}

.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }
</style>
