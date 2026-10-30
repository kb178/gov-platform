import request from './index'

// ==================== 用户管理 ====================

// 用户分页列表（管理端）
export function getUserPage(params) {
  return request({
    url: '/system/sysUser/admin/page',
    method: 'get',
    params
  })
}

// 用户详情（管理端）
export function getUserDetail(userId) {
  return request({
    url: `/system/sysUser/admin/${userId}`,
    method: 'get'
  })
}

// 新增用户（管理端）
export function addUser(data) {
  return request({
    url: '/system/sysUser/admin',
    method: 'post',
    data
  })
}

// 修改用户（管理端）
export function updateUser(data) {
  return request({
    url: '/system/sysUser/admin',
    method: 'put',
    data
  })
}

// 删除用户（管理端）
export function deleteUser(userId) {
  return request({
    url: `/system/sysUser/admin/${userId}`,
    method: 'delete'
  })
}

// 管理员重置密码
export function resetUserPassword(userId) {
  return request({
    url: `/system/sysUser/admin/resetPassword/${userId}`,
    method: 'put'
  })
}

// 分配用户角色
export function assignUserRoles(userId, roleIds) {
  return request({
    url: `/system/sysUser/admin/assignRoles/${userId}`,
    method: 'put',
    data: roleIds
  })
}

// ==================== 角色管理 ====================

// 查询角色列表
export function getRoleList() {
  return request({
    url: '/system/sysRole/list',
    method: 'get'
  })
}

// 查询角色详情
export function getRoleDetail(roleId) {
  return request({
    url: `/system/sysRole/${roleId}`,
    method: 'get'
  })
}

// 新增角色
export function addRole(data) {
  return request({
    url: '/system/sysRole',
    method: 'post',
    data
  })
}

// 修改角色
export function updateRole(data) {
  return request({
    url: '/system/sysRole',
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(roleId) {
  return request({
    url: `/system/sysRole/${roleId}`,
    method: 'delete'
  })
}

// 分配菜单权限
export function assignRoleMenus(roleId, menuIds) {
  return request({
    url: `/system/sysRole/${roleId}/menus`,
    method: 'put',
    data: menuIds
  })
}

// ==================== 菜单管理 ====================

// 查询菜单列表（树形）
export function getMenuList() {
  return request({
    url: '/system/sysMenu/list',
    method: 'get'
  })
}

// 查询菜单详情
export function getMenuDetail(menuId) {
  return request({
    url: `/system/sysMenu/${menuId}`,
    method: 'get'
  })
}

// 新增菜单
export function addMenu(data) {
  return request({
    url: '/system/sysMenu',
    method: 'post',
    data
  })
}

// 修改菜单
export function updateMenu(data) {
  return request({
    url: '/system/sysMenu',
    method: 'put',
    data
  })
}

// 删除菜单
export function deleteMenu(menuId) {
  return request({
    url: `/system/sysMenu/${menuId}`,
    method: 'delete'
  })
}

// ==================== 部门管理 ====================

// 查询部门列表（树形）
export function getDeptList() {
  return request({
    url: '/system/sysDept/list',
    method: 'get'
  })
}

// 查询部门详情
export function getDeptDetail(deptId) {
  return request({
    url: `/system/sysDept/${deptId}`,
    method: 'get'
  })
}

// 新增部门
export function addDept(data) {
  return request({
    url: '/system/sysDept',
    method: 'post',
    data
  })
}

// 修改部门
export function updateDept(data) {
  return request({
    url: '/system/sysDept',
    method: 'put',
    data
  })
}

// 删除部门
export function deleteDept(deptId) {
  return request({
    url: `/system/sysDept/${deptId}`,
    method: 'delete'
  })
}

// ==================== 字典类型管理 ====================

// 查询字典类型列表
export function getDictTypeList() {
  return request({
    url: '/system/dictType/list',
    method: 'get'
  })
}

// 查询字典类型详情
export function getDictTypeDetail(dictId) {
  return request({
    url: `/system/dictType/${dictId}`,
    method: 'get'
  })
}

// 新增字典类型
export function addDictType(data) {
  return request({
    url: '/system/dictType',
    method: 'post',
    data
  })
}

// 修改字典类型
export function updateDictType(data) {
  return request({
    url: '/system/dictType',
    method: 'put',
    data
  })
}

// 删除字典类型
export function deleteDictType(dictId) {
  return request({
    url: `/system/dictType/${dictId}`,
    method: 'delete'
  })
}

// ==================== 字典数据管理 ====================

// 根据字典类型查询字典数据列表
export function getDictDataByType(dictType) {
  return request({
    url: `/system/dictData/type/${dictType}`,
    method: 'get'
  })
}

// 查询字典数据详情
export function getDictDataDetail(dictCode) {
  return request({
    url: `/system/dictData/${dictCode}`,
    method: 'get'
  })
}

// 新增字典数据
export function addDictData(data) {
  return request({
    url: '/system/dictData',
    method: 'post',
    data
  })
}

// 修改字典数据
export function updateDictData(data) {
  return request({
    url: '/system/dictData',
    method: 'put',
    data
  })
}

// 删除字典数据
export function deleteDictData(dictCode) {
  return request({
    url: `/system/dictData/${dictCode}`,
    method: 'delete'
  })
}

// ==================== 系统参数管理 ====================

// 分页查询系统参数
export function getConfigPage(params) {
  return request({
    url: '/system/sysConfig/page',
    method: 'get',
    params
  })
}

// 查询系统参数列表
export function getConfigList() {
  return request({
    url: '/system/sysConfig/list',
    method: 'get'
  })
}

// 查询系统参数详情
export function getConfigDetail(configId) {
  return request({
    url: `/system/sysConfig/${configId}`,
    method: 'get'
  })
}

// 根据键名获取参数值
export function getConfigByKey(configKey) {
  return request({
    url: `/system/sysConfig/key/${configKey}`,
    method: 'get'
  })
}

// 新增系统参数
export function addConfig(data) {
  return request({
    url: '/system/sysConfig',
    method: 'post',
    data
  })
}

// 修改系统参数
export function updateConfig(data) {
  return request({
    url: '/system/sysConfig',
    method: 'put',
    data
  })
}

// 删除系统参数
export function deleteConfig(configId) {
  return request({
    url: `/system/sysConfig/${configId}`,
    method: 'delete'
  })
}

// ==================== 登录日志管理 ====================

// 分页查询登录日志
export function getLoginLogPage(params) {
  return request({
    url: '/system/sysLoginLog/page',
    method: 'get',
    params
  })
}

// 查询登录日志列表
export function getLoginLogList() {
  return request({
    url: '/system/sysLoginLog/list',
    method: 'get'
  })
}

// 查询登录日志详情
export function getLoginLogDetail(infoId) {
  return request({
    url: `/system/sysLoginLog/${infoId}`,
    method: 'get'
  })
}

// 清空登录日志
export function cleanLoginLog() {
  return request({
    url: '/system/sysLoginLog/clean',
    method: 'delete'
  })
}

// ==================== 操作日志管理 ====================

// 分页查询操作日志
export function getOperLogPage(params) {
  return request({
    url: '/system/sysOperLog/page',
    method: 'get',
    params
  })
}

// 查询操作日志列表
export function getOperLogList() {
  return request({
    url: '/system/sysOperLog/list',
    method: 'get'
  })
}

// 查询操作日志详情
export function getOperLogDetail(operId) {
  return request({
    url: `/system/sysOperLog/${operId}`,
    method: 'get'
  })
}

// 清空操作日志
export function cleanOperLog() {
  return request({
    url: '/system/sysOperLog/clean',
    method: 'delete'
  })
}
