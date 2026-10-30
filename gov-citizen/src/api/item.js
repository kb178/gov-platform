import request from './index'

// ========== 事项分类 ==========

/**
 * 获取分类树形列表
 */
export function getCategoryTree() {
  return request({
    url: '/item/itemCategory/tree',
    method: 'get'
  })
}

/**
 * 获取分类列表
 * @param {Long} parentId 父分类ID（可选）
 */
export function getCategoryList(parentId) {
  return request({
    url: '/item/itemCategory/list',
    method: 'get',
    params: { parentId }
  })
}

// ========== 事项信息 ==========

/**
 * 分页查询事项列表
 * @param {Object} params 查询参数
 */
export function getItemList(params) {
  return request({
    url: '/item/itemInfo/list',
    method: 'get',
    params
  })
}

/**
 * 查询事项详情
 * @param {Long} itemId 事项ID
 */
export function getItemDetail(itemId) {
  return request({
    url: `/item/itemInfo/${itemId}`,
    method: 'get'
  })
}

// ========== 事项收藏 ==========

/**
 * 收藏事项
 * @param {Object} data { userId, itemId }
 */
export function addFavorite(data) {
  return request({
    url: '/item/itemFavorite',
    method: 'post',
    data
  })
}

/**
 * 取消收藏
 * @param {Object} data { userId, itemId }
 */
export function removeFavorite(data) {
  return request({
    url: '/item/itemFavorite',
    method: 'delete',
    data
  })
}

/**
 * 查询是否已收藏
 * @param {Long} userId 用户ID
 * @param {Long} itemId 事项ID
 */
export function checkFavorite(userId, itemId) {
  return request({
    url: '/item/itemFavorite/check',
    method: 'get',
    params: { userId, itemId }
  })
}

/**
 * 查询用户的收藏列表
 * @param {Object} params { pageNum, pageSize, userId }
 */
export function getFavoriteList(params) {
  return request({
    url: '/item/itemFavorite/list',
    method: 'get',
    params
  })
}

// ========== 事项材料 ==========

/**
 * 查询事项的材料列表
 * @param {Long} itemId 事项ID
 */
export function getItemMaterials(itemId) {
  return request({
    url: `/item/itemMaterialRelation/item/${itemId}`,
    method: 'get'
  })
}

// ========== 表单模板 ==========

/**
 * 获取事项当前启用的表单模板
 * @param {Long} itemId 事项ID
 */
export function getActiveFormTemplate(itemId) {
  return request({
    url: `/item/itemFormTemplate/active/${itemId}`,
    method: 'get'
  })
}
