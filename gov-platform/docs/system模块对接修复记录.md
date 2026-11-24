# system 模块对接修复记录

> 2026-11-24 ｜ 背景：gov-system 后端接口与 gov-admin 前端「系统管理」模块对接核查，修复字段缺失、命名不一致、业务缺陷等问题共 10 项。

---

## 一、改动总览

| # | 问题 | 定性 | 改动位置 |
|---|------|------|---------|
| 1 | 分页响应字段 `rows`，前端期望 `records` | 命名不一致 | `PageResult` |
| 2 | UserVO 的 `deptName` 恒 null、`roleNames` 恒空 | 实现缺陷 | `SysUserServiceImpl` |
| 3 | 列表手机号脱敏，编辑回存会把脱敏值写回库 | 数据污染风险 | `SysUserServiceImpl` |
| 4 | 新增用户必须传密码，前端表单没有密码框 | 校验过严 | `UserAddDTO` / `SysUserServiceImpl` |
| 5 | 管理端建的账号默认无 `userType`，登不了管理端 | 默认值缺失 | `SysUserServiceImpl` |
| 6 | 编辑用户提交的 `roleIds` 被丢弃，角色改不了 | 功能缺失 | `UserUpdateDTO` / `SysUserServiceImpl` |
| 7 | UserVO 缺 `remark`，编辑弹窗回显为空 | 字段缺失 | `UserVO` |
| 8 | `sysConfig` 缺 `configType`（系统内置标志） | 功能缺失 | 实体/DTO/VO/查询 + DB |
| 9 | 操作日志缺 `businessType`（业务操作类型） | 功能缺失 | 实体/VO/查询/LogAspect + DB |
| 10 | 日志查询 `beginTime/endTime` 绑定失败（前端传 `yyyy-MM-dd`） | 参数绑定缺陷 | `OperLogQueryDTO` / `LoginLogQueryDTO` |

---

## 二、改动明细

### 2.1 分页结构（gov-common-core）

`PageResult` 字段 `rows` 更名为 `records`（与 MyBatis-Plus IPage 序列化风格一致，前端统一只读 `records`）：

```json
{ "total": 100, "records": [ ... ], "pageNum": 1, "pageSize": 10 }
```

> 影响范围：所有返回 `PageResult` 的接口（system/item/approval 等）。项目内无 `getRows()` 调用点，改名无连锁影响。

### 2.2 用户管理（SysUserServiceImpl / UserVO / DTO）

- **convertToUserVO 补全**
  - `deptName`：按 `deptId` 查 `sys_dept` 回填（此前从不赋值）
  - `roleNames`：按 `roleIds` 查 `sys_role` 回填（此前恒为空数组）
  - `remark`：UserVO 新增该字段并回填，供编辑弹窗回显
  - `phone`：管理端不再脱敏。脱敏值被编辑表单回显后保存会污染库，脱敏仅保留在个人中心 `UserInfoVO`
- **addUser 行为调整**
  - `password` 可为空，为空时默认 `123456`（与管理员重置密码、`sys.user.initPassword` 配置一致）
  - `userType` 为空时默认 `2`（工作人员）。管理端创建的是工作人员账号，`adminLogin` 要求 `userType=2`，否则报「该账号无管理权限」
- **updateUser 支持角色分配**
  - `UserUpdateDTO` 新增 `List<Long> roleIds`
  - 语义：`roleIds == null` 不更新（如只改状态的局部更新）；空数组清空角色；非空全量覆盖（先删 `user_role` 再插）
- **UserAddDTO**：`password` 去掉 `@NotBlank`（保留长度校验，仅在显式传入时生效）

### 2.3 系统参数 configType（SysConfig 链路）

前端用 `configType`（`Y` 内置 / `N` 非内置）控制「内置参数禁止删除」，后端此前无此字段。全链路补齐：

- `sys_config` 表新增 `config_type CHAR(1) NOT NULL DEFAULT 'N'`（存量数据已置 `Y`）
- `SysConfig` 实体、`ConfigDTO`、`ConfigVO`、`ConfigQueryDTO` 新增 `configType`
- `SysConfigServiceImpl.buildQueryWrapper` 支持按 `configType` 精确筛选

### 2.4 操作日志 businessType（SysOperLog 链路）

`@Log` 注解早已携带 `businessType`，但落库时被并进 `title` 文本后丢弃。改为独立存储：

- `sys_oper_log` 表新增 `business_type TINYINT DEFAULT 0`
- `BusinessType` 枚举增加 `code`，与前端操作日志页的类型编号对齐：

  | code | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
  |------|---|---|---|---|---|---|---|
  | 含义 | 其他 | 新增 | 修改 | 删除 | 查询 | 授权 | 导出 |

- `SysOperLog`、`OperLogVO`、`OperLogQueryDTO` 新增 `businessType`；查询支持按类型筛选
- `LogAspect`：`businessType` 独立入库；`title` 只记模块名（原先拼接成 `用户管理 - 新增`，与独立的类型列重复）

### 2.5 日志时间查询参数绑定

`OperLogQueryDTO` / `LoginLogQueryDTO` 的 `beginTime`、`endTime` 为 `LocalDateTime`，无格式注解时 Spring 按 ISO 解析，前端传 `yyyy-MM-dd` 直接绑定失败。加注解固定格式：

```java
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime beginTime;
```

前端相应传 `2026-11-24 00:00:00` / `23:59:59`（date-range 选择器补时分秒）。

---

## 三、数据库变更

```sql
-- gov_system.sys_config
ALTER TABLE sys_config ADD COLUMN config_type CHAR(1) NOT NULL DEFAULT 'N'
  COMMENT '系统内置（Y是 N否）' AFTER config_value;
UPDATE sys_config SET config_type = 'Y';

-- gov_system.sys_oper_log
ALTER TABLE sys_oper_log ADD COLUMN business_type TINYINT DEFAULT 0
  COMMENT '业务操作类型（0其他 1新增 2修改 3删除 4查询 5授权 6导出）' AFTER title;
```

---

## 四、前端配套改动（gov-admin）

后端字段命名与实体/库表映射绑定，故命名类问题由前端适配后端：

| 位置 | 改动 |
|------|------|
| `vite.config.js` | 代理 `/system`、`/item` 前缀收窄为 `/system/sys*`、`/system/dictType|dictData`、`/item/item*`，避免页面路由（如 `/system/user`）的整页请求被转发到网关拿到 401 |
| `views/system/user.vue` | `userName/nickName/roles` → `username/nickname/roleIds/roleNames` |
| `views/system/role.vue` `menu.vue` `dept.vue` | `sort` → `sortNum` |
| `views/system/dict.vue` | `dictSort` → `sortNum` |
| `views/system/log.vue` | 日期参数补时分秒；`typeTextMap` 增加 `0: 其他`；修复 `#default="{ row }}"` 多余括号导致的 Vue 编译失败（原有 bug，操作日志页此前整页白屏） |
| `src/api/index.js` | 错误提示兼容后端 `msg` 字段（原先只读 `message`，报错被吞成「请求失败」） |

---

## 五、验证记录

- 接口实测：用户分页/新增（免密默认 123456）/编辑（roleIds 同步）/重置密码、配置分页（configType）、操作日志分页（businessType）与筛选、日志日期区间筛选，全部通过
- 浏览器实测（admin/admin123）：用户、角色、部门、菜单、字典、操作日志、系统参数 7 个页面渲染正常；用户编辑弹窗回显（用户名禁用、部门、角色多选、备注）正确
