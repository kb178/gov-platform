import request from './index'

// ==================== 待办 / 已办 ====================

// 待我审批
export function getTodoList(params) {
  return request({
    url: '/approval/approval/todo',
    method: 'get',
    params
  })
}

// 已办列表
export function getDoneList(params) {
  return request({
    url: '/approval/approval/done',
    method: 'get',
    params
  })
}

// ==================== 审批操作 ====================

// 受理
export function accept(data) {
  return request({
    url: '/approval/approval/accept',
    method: 'post',
    data
  })
}

// 不予受理
export function rejectAtAccept(data) {
  return request({
    url: '/approval/approval/rejectAtAccept',
    method: 'post',
    data
  })
}

// 审核通过
export function reviewPass(data) {
  return request({
    url: '/approval/approval/reviewPass',
    method: 'post',
    data
  })
}

// 审批通过
export function approve(data) {
  return request({
    url: '/approval/approval/approve',
    method: 'post',
    data
  })
}

// 驳回
export function reject(data) {
  return request({
    url: '/approval/approval/reject',
    method: 'post',
    data
  })
}

// 退回补材料
export function returnForMaterial(data) {
  return request({
    url: '/approval/approval/return',
    method: 'post',
    data
  })
}

// ==================== 详情 / 记录 ====================

// 申请详情
export function getApplicationDetail(applicationId) {
  return request({
    url: `/approval/application/${applicationId}`,
    method: 'get'
  })
}

// 审批记录
export function getApprovalRecords(applicationId) {
  return request({
    url: `/approval/application/record/${applicationId}`,
    method: 'get'
  })
}

// 审批进度时间线
export function getProgress(applicationId) {
  return request({
    url: `/approval/approval/progress/${applicationId}`,
    method: 'get'
  })
}

// 材料核验
export function checkMaterials(data) {
  return request({
    url: '/approval/approval/material/check',
    method: 'post',
    data
  })
}
