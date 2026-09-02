import request from './index'

// ==================== 事项信息管理 ====================

// 分页查询事项列表
export function getItemList(params) {
  return request({
    url: '/item/itemInfo/list',
    method: 'get',
    params
  })
}

// 查询事项详情
export function getItemDetail(itemId) {
  return request({
    url: `/item/itemInfo/${itemId}`,
    method: 'get'
  })
}

// 新增事项
export function addItem(data) {
  return request({
    url: '/item/itemInfo',
    method: 'post',
    data
  })
}

// 修改事项
export function updateItem(data) {
  return request({
    url: '/item/itemInfo',
    method: 'put',
    data
  })
}

// 删除事项
export function deleteItem(itemId) {
  return request({
    url: `/item/itemInfo/${itemId}`,
    method: 'delete'
  })
}

// 发布事项
export function publishItem(itemId) {
  return request({
    url: `/item/itemInfo/publish/${itemId}`,
    method: 'put'
  })
}

// 下线事项
export function offlineItem(itemId) {
  return request({
    url: `/item/itemInfo/offline/${itemId}`,
    method: 'put'
  })
}

// ==================== 事项分类管理 ====================

// 查询分类树形列表
export function getCategoryTree() {
  return request({
    url: '/item/itemCategory/tree',
    method: 'get'
  })
}

// 查询分类列表
export function getCategoryList(parentId) {
  return request({
    url: '/item/itemCategory/list',
    method: 'get',
    params: { parentId }
  })
}

// 查询分类详情
export function getCategoryDetail(categoryId) {
  return request({
    url: `/item/itemCategory/${categoryId}`,
    method: 'get'
  })
}

// 新增分类
export function addCategory(data) {
  return request({
    url: '/item/itemCategory',
    method: 'post',
    data
  })
}

// 修改分类
export function updateCategory(data) {
  return request({
    url: '/item/itemCategory',
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(categoryId) {
  return request({
    url: `/item/itemCategory/${categoryId}`,
    method: 'delete'
  })
}

// ==================== 事项收藏管理 ====================

// 收藏事项
export function addItemFavorite(data) {
  return request({
    url: '/item/itemFavorite',
    method: 'post',
    data
  })
}

// 取消收藏
export function removeItemFavorite(data) {
  return request({
    url: '/item/itemFavorite',
    method: 'delete',
    data
  })
}

// 查询是否已收藏
export function checkItemFavorite(userId, itemId) {
  return request({
    url: '/item/itemFavorite/check',
    method: 'get',
    params: { userId, itemId }
  })
}

// 查询用户收藏列表（分页）
export function getItemFavoriteList(params) {
  return request({
    url: '/item/itemFavorite/list',
    method: 'get',
    params
  })
}

// ==================== 表单模板管理 ====================

// 查询事项的表单模板列表
export function getFormTemplateByItem(itemId) {
  return request({
    url: `/item/itemFormTemplate/item/${itemId}`,
    method: 'get'
  })
}

// 查询模板详情
export function getFormTemplateDetail(templateId) {
  return request({
    url: `/item/itemFormTemplate/${templateId}`,
    method: 'get'
  })
}

// 获取事项当前启用的模板
export function getActiveFormTemplate(itemId) {
  return request({
    url: `/item/itemFormTemplate/active/${itemId}`,
    method: 'get'
  })
}

// 新增模板
export function addFormTemplate(data) {
  return request({
    url: '/item/itemFormTemplate',
    method: 'post',
    data
  })
}

// 修改模板
export function updateFormTemplate(data) {
  return request({
    url: '/item/itemFormTemplate',
    method: 'put',
    data
  })
}

// 删除模板
export function deleteFormTemplate(templateId) {
  return request({
    url: `/item/itemFormTemplate/${templateId}`,
    method: 'delete'
  })
}

// 启用模板
export function enableFormTemplate(templateId) {
  return request({
    url: `/item/itemFormTemplate/enable/${templateId}`,
    method: 'put'
  })
}

// 禁用模板
export function disableFormTemplate(templateId) {
  return request({
    url: `/item/itemFormTemplate/disable/${templateId}`,
    method: 'put'
  })
}

// ==================== 材料模板管理 ====================

// 分页查询材料模板列表
export function getMaterialTemplateList(params) {
  return request({
    url: '/item/itemMaterialTemplate/list',
    method: 'get',
    params
  })
}

// 查询材料模板详情
export function getMaterialTemplateDetail(materialId) {
  return request({
    url: `/item/itemMaterialTemplate/${materialId}`,
    method: 'get'
  })
}

// 查询所有可用材料模板
export function getActiveMaterialTemplates() {
  return request({
    url: '/item/itemMaterialTemplate/active',
    method: 'get'
  })
}

// 新增材料模板
export function addMaterialTemplate(data) {
  return request({
    url: '/item/itemMaterialTemplate',
    method: 'post',
    data
  })
}

// 修改材料模板
export function updateMaterialTemplate(data) {
  return request({
    url: '/item/itemMaterialTemplate',
    method: 'put',
    data
  })
}

// 删除材料模板
export function deleteMaterialTemplate(materialId) {
  return request({
    url: `/item/itemMaterialTemplate/${materialId}`,
    method: 'delete'
  })
}

// ==================== 事项材料关联管理 ====================

// 查询事项的材料列表
export function getMaterialsByItem(itemId) {
  return request({
    url: `/item/itemMaterialRelation/item/${itemId}`,
    method: 'get'
  })
}

// 批量设置事项的材料
export function setItemMaterials(data) {
  return request({
    url: '/item/itemMaterialRelation/set',
    method: 'post',
    data
  })
}

// 删除材料关联
export function deleteMaterialRelation(id) {
  return request({
    url: `/item/itemMaterialRelation/${id}`,
    method: 'delete'
  })
}

// 删除事项的所有材料关联
export function deleteAllItemMaterials(itemId) {
  return request({
    url: `/item/itemMaterialRelation/item/${itemId}`,
    method: 'delete'
  })
}
