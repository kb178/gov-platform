-- =============================================
-- 政务一体化平台 - 第一阶段建表语句
-- 共 11 张表
-- =============================================

-- ----------------------------
-- 1. 用户表
-- ----------------------------
CREATE TABLE `sys_user` (
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `password` VARCHAR(128) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(64) DEFAULT '' COMMENT '昵称',
  `real_name` VARCHAR(64) DEFAULT '' COMMENT '真实姓名',
  `id_card` VARCHAR(18) DEFAULT '' COMMENT '身份证号',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
  `email` VARCHAR(128) DEFAULT '' COMMENT '邮箱',
  `sex` TINYINT DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '头像地址',
  `user_type` TINYINT DEFAULT 1 COMMENT '用户类型（1老百姓 2工作人员）',
  `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID（工作人员才有）',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `real_name_status` TINYINT DEFAULT 0 COMMENT '实名认证状态（0未认证 1已认证）',
  `login_ip` VARCHAR(128) DEFAULT '' COMMENT '最后登录IP',
  `login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `idx_username` (`username`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB COMMENT='用户表';

-- ----------------------------
-- 2. 角色表
-- ----------------------------
CREATE TABLE `sys_role` (
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `role_key` VARCHAR(100) NOT NULL COMMENT '角色标识',
  `sort_num` INT DEFAULT 0 COMMENT '排序',
  `data_scope` TINYINT DEFAULT 1 COMMENT '数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB COMMENT='角色表';

-- ----------------------------
-- 3. 用户角色关联表
-- ----------------------------
CREATE TABLE `user_role` (
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- ----------------------------
-- 4. 菜单表
-- ----------------------------
CREATE TABLE `sys_menu` (
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `menu_name` VARCHAR(64) NOT NULL COMMENT '菜单名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID',
  `sort_num` INT DEFAULT 0 COMMENT '排序',
  `path` VARCHAR(200) DEFAULT '' COMMENT '路由地址',
  `component` VARCHAR(255) DEFAULT '' COMMENT '组件路径',
  `menu_type` CHAR(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `perms` VARCHAR(100) DEFAULT '' COMMENT '权限标识',
  `icon` VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
  `visible` TINYINT DEFAULT 0 COMMENT '是否可见（0可见 1隐藏）',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB COMMENT='菜单表';

-- ----------------------------
-- 5. 角色菜单关联表
-- ----------------------------
CREATE TABLE `role_menu` (
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB COMMENT='角色菜单关联表';

-- ----------------------------
-- 6. 部门表
-- ----------------------------
CREATE TABLE `sys_dept` (
  `dept_id` BIGINT NOT NULL COMMENT '部门ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父部门ID',
  `ancestors` VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
  `dept_name` VARCHAR(64) NOT NULL COMMENT '部门名称',
  `sort_num` INT DEFAULT 0 COMMENT '排序',
  `leader` VARCHAR(64) DEFAULT '' COMMENT '负责人',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '联系电话',
  `email` VARCHAR(128) DEFAULT '' COMMENT '邮箱',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB COMMENT='部门表';

-- ----------------------------
-- 7. 字典类型表
-- ----------------------------
CREATE TABLE `dict_type` (
  `dict_id` BIGINT NOT NULL COMMENT '字典类型ID',
  `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
  `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB COMMENT='字典类型表';

-- ----------------------------
-- 8. 字典数据表
-- ----------------------------
CREATE TABLE `dict_data` (
  `dict_code` BIGINT NOT NULL COMMENT '字典编码',
  `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
  `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签',
  `dict_value` VARCHAR(100) NOT NULL COMMENT '字典值',
  `sort_num` INT DEFAULT 0 COMMENT '排序',
  `css_class` VARCHAR(100) DEFAULT '' COMMENT '样式属性',
  `list_class` VARCHAR(100) DEFAULT '' COMMENT '表格回显样式',
  `is_default` CHAR(1) DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dict_code`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB COMMENT='字典数据表';

-- ----------------------------
-- 9. 操作日志表
-- ----------------------------
CREATE TABLE `sys_oper_log` (
  `oper_id` BIGINT NOT NULL COMMENT '日志ID',
  `title` VARCHAR(50) DEFAULT '' COMMENT '模块标题',
  `method` VARCHAR(200) DEFAULT '' COMMENT '方法名称',
  `request_method` VARCHAR(10) DEFAULT '' COMMENT '请求方式',
  `oper_name` VARCHAR(50) DEFAULT '' COMMENT '操作人员',
  `oper_url` VARCHAR(500) DEFAULT '' COMMENT '请求URL',
  `oper_ip` VARCHAR(128) DEFAULT '' COMMENT '操作IP',
  `oper_param` TEXT COMMENT '请求参数',
  `json_result` TEXT COMMENT '返回参数',
  `status` TINYINT DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` TEXT COMMENT '错误消息',
  `oper_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `cost_time` BIGINT DEFAULT 0 COMMENT '耗时（毫秒）',
  PRIMARY KEY (`oper_id`),
  KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB COMMENT='操作日志表';

-- ----------------------------
-- 10. 登录日志表
-- ----------------------------
CREATE TABLE `sys_login_log` (
  `info_id` BIGINT NOT NULL COMMENT '日志ID',
  `username` VARCHAR(64) DEFAULT '' COMMENT '用户名',
  `ipaddr` VARCHAR(128) DEFAULT '' COMMENT '登录IP',
  `login_location` VARCHAR(255) DEFAULT '' COMMENT '登录地点',
  `browser` VARCHAR(50) DEFAULT '' COMMENT '浏览器类型',
  `os` VARCHAR(50) DEFAULT '' COMMENT '操作系统',
  `status` TINYINT DEFAULT 0 COMMENT '登录状态（0成功 1失败）',
  `msg` VARCHAR(255) DEFAULT '' COMMENT '提示消息',
  `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`info_id`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB COMMENT='登录日志表';

-- ----------------------------
-- 11. 系统参数表
-- ----------------------------
CREATE TABLE `sys_config` (
  `config_id` BIGINT NOT NULL COMMENT '参数ID',
  `config_name` VARCHAR(100) NOT NULL COMMENT '参数名称',
  `config_key` VARCHAR(100) NOT NULL COMMENT '参数键名',
  `config_value` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '参数键值',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `idx_config_key` (`config_key`)
) ENGINE=InnoDB COMMENT='系统参数表';


-- =============================================
-- 初始数据
-- =============================================

-- 超级管理员（密码：admin123）
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `nickname`, `user_type`, `status`)
VALUES (1, 'admin', '$2a$10$VQECfCqt3FI8MnkzIUCqH.sECWGJDFv5O3MJ3mORlBoVx3iYXBwwS', '超级管理员', 2, 0);

-- 默认角色
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `sort_num`)
VALUES (1, '超级管理员', 'admin', 1),
       (2, '普通管理员', 'manager', 2),
       (3, '窗口人员', 'window', 3),
       (4, '普通用户', 'user', 4);

-- 默认部门
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `dept_name`, `sort_num`)
VALUES (100, 0, '海口市政务服务中心', 1),
       (101, 100, '综合窗口', 1),
       (102, 100, '户籍管理科', 2),
       (103, 100, '社保服务科', 3),
       (104, 100, '工商登记科', 4);

-- 默认菜单
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `sort_num`, `path`, `component`, `menu_type`, `perms`, `icon`) VALUES
-- 一级目录
(1, '系统管理', 0, 1, 'system', '', 'M', '', 'setting'),
(2, '审批管理', 0, 2, 'approval', '', 'M', '', 'check'),
(3, '事项管理', 0, 3, 'item', '', 'M', '', 'list'),
(4, '证照管理', 0, 4, 'license', '', 'M', '', 'ticket'),
(5, '数据统计', 0, 5, 'data', '', 'M', '', 'chart'),
(6, '消息中心', 0, 6, 'message', '', 'M', '', 'bell'),
-- 系统管理子菜单
(100, '用户管理', 1, 1, 'user', 'system/user/index', 'C', 'system:user:list', 'user'),
(101, '角色管理', 1, 2, 'role', 'system/role/index', 'C', 'system:role:list', 'peoples'),
(102, '部门管理', 1, 3, 'dept', 'system/dept/index', 'C', 'system:dept:list', 'tree'),
(103, '菜单管理', 1, 4, 'menu', 'system/menu/index', 'C', 'system:menu:list', 'menu'),
(104, '字典管理', 1, 5, 'dict', 'system/dict/index', 'C', 'system:dict:list', 'dict'),
(105, '系统参数', 1, 6, 'config', 'system/config/index', 'C', 'system:config:list', 'edit'),
(106, '操作日志', 1, 7, 'operlog', 'system/operlog/index', 'C', 'system:operlog:list', 'form'),
(107, '登录日志', 1, 8, 'loginlog', 'system/loginlog/index', 'C', 'system:loginlog:list', 'logininfor'),
-- 审批管理子菜单
(200, '待我审批', 2, 1, 'todo', 'approval/todo/index', 'C', 'approval:todo:list', 'edit'),
(201, '已办事项', 2, 2, 'done', 'approval/done/index', 'C', 'approval:done:list', 'finish'),
(202, '流程监控', 2, 3, 'monitor', 'approval/monitor/index', 'C', 'approval:monitor:list', 'monitor'),
-- 事项管理子菜单
(300, '事项分类', 3, 1, 'category', 'item/category/index', 'C', 'item:category:list', 'classify'),
(301, '事项列表', 3, 2, 'list', 'item/list/index', 'C', 'item:list:list', 'list'),
-- 用户管理按钮权限
(1001, '用户查询', 100, 1, '', '', 'F', 'system:user:query', '#'),
(1002, '用户新增', 100, 2, '', '', 'F', 'system:user:add', '#'),
(1003, '用户修改', 100, 3, '', '', 'F', 'system:user:edit', '#'),
(1004, '用户删除', 100, 4, '', '', 'F', 'system:user:remove', '#'),
-- 角色管理按钮权限
(1011, '角色查询', 101, 1, '', '', 'F', 'system:role:query', '#'),
(1012, '角色新增', 101, 2, '', '', 'F', 'system:role:add', '#'),
(1013, '角色修改', 101, 3, '', '', 'F', 'system:role:edit', '#'),
(1014, '角色删除', 101, 4, '', '', 'F', 'system:role:remove', '#');

-- 超级管理员拥有所有菜单权限
INSERT INTO `role_menu` (`role_id`, `menu_id`)
SELECT 1, `menu_id` FROM `sys_menu`;

-- 字典类型初始数据
INSERT INTO `dict_type` (`dict_id`, `dict_name`, `dict_type`) VALUES
(1, '用户性别', 'sys_user_sex'),
(2, '系统状态', 'sys_normal_disable'),
(3, '用户类型', 'sys_user_type'),
(4, '菜单类型', 'sys_menu_type'),
(5, '数据范围', 'sys_data_scope'),
(6, '操作状态', 'sys_oper_status'),
(7, '登录状态', 'sys_login_status'),
(8, '实名认证状态', 'sys_real_name_status');

-- 字典数据初始数据
INSERT INTO `dict_data` (`dict_code`, `dict_type`, `dict_label`, `dict_value`, `sort_num`) VALUES
-- 用户性别
(100, 'sys_user_sex', '未知', '0', 1),
(101, 'sys_user_sex', '男', '1', 2),
(102, 'sys_user_sex', '女', '2', 3),
-- 系统状态
(200, 'sys_normal_disable', '正常', '0', 1),
(201, 'sys_normal_disable', '停用', '1', 2),
-- 用户类型
(300, 'sys_user_type', '老百姓', '1', 1),
(301, 'sys_user_type', '工作人员', '2', 2),
-- 菜单类型
(400, 'sys_menu_type', '目录', 'M', 1),
(401, 'sys_menu_type', '菜单', 'C', 2),
(402, 'sys_menu_type', '按钮', 'F', 3),
-- 数据范围
(500, 'sys_data_scope', '全部数据', '1', 1),
(501, 'sys_data_scope', '自定义数据', '2', 2),
(502, 'sys_data_scope', '本部门数据', '3', 3),
(503, 'sys_data_scope', '本部门及以下', '4', 4),
(504, 'sys_data_scope', '仅本人数据', '5', 5),
-- 操作状态
(600, 'sys_oper_status', '成功', '0', 1),
(601, 'sys_oper_status', '失败', '1', 2),
-- 登录状态
(700, 'sys_login_status', '成功', '0', 1),
(701, 'sys_login_status', '失败', '1', 2),
-- 实名认证状态
(800, 'sys_real_name_status', '未认证', '0', 1),
(801, 'sys_real_name_status', '已认证', '1', 2);

-- 系统参数初始数据
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `remark`) VALUES
(1, '用户初始密码', 'sys.user.initPassword', '123456', '用户初始密码'),
(2, '账号重复注册', 'sys.account.repeatRegister', 'false', '是否允许重复注册'),
(3, '验证码开关', 'sys.captcha.enabled', 'true', '是否开启验证码'),
(4, '短信验证码有效期', 'sys.sms.expireTime', '5', '短信验证码有效期（分钟）'),
(5, '文件上传大小', 'sys.file.maxSize', '10', '文件上传大小限制（MB）');
