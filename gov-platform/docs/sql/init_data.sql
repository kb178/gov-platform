-- =============================================
-- 政务一体化平台 - 初始化数据（整合版）
-- 合并自：init_admin.sql / init_admin_safe.sql / init_admin_v2.sql / insert_users.sql
--
-- 执行前请确保已执行：第一阶段-系统基础表.sql
-- 执行命令：mysql -u root -p --default-character-set=utf8mb4 gov_system < init_data.sql
--
-- 账号信息：
--   管理员：admin / admin123（管理端登录）
--   测试用户：13800000001 / admin123（用户端登录）
-- =============================================

-- ----------------------------
-- 1. 清理旧数据（可重复执行）
-- ----------------------------
DELETE FROM `user_role` WHERE `user_id` IN (1, 2);
DELETE FROM `role_menu` WHERE `role_id` = 1;

-- ----------------------------
-- 2. 初始化角色
-- ----------------------------
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `sort_num`, `data_scope`, `status`, `remark`) VALUES
(1, '超级管理员', 'admin',    1, 1, 0, '拥有所有权限'),
(2, '普通管理员', 'manager',  2, 1, 0, '部分管理权限'),
(3, '窗口人员',   'window',   3, 1, 0, '审批处理人员'),
(4, '普通用户',   'user',     4, 1, 0, '普通办事群众')
ON DUPLICATE KEY UPDATE
  `role_name` = VALUES(`role_name`),
  `role_key`  = VALUES(`role_key`),
  `status`    = 0,
  `del_flag`  = 0;

-- ----------------------------
-- 3. 初始化用户
-- ----------------------------

-- 3.1 管理员（用户名: admin，密码: admin123）
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `nickname`, `real_name`, `phone`, `user_type`, `status`, `remark`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', '系统管理员', '13800000000', 2, 0, '最高权限管理员')
ON DUPLICATE KEY UPDATE
  `username` = 'admin',
  `password` = '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
  `phone`    = '13800000000',
  `user_type` = 2,
  `status`   = 0,
  `del_flag` = 0;

-- 3.2 测试用户（手机号: 13800000001，密码: admin123）
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `nickname`, `real_name`, `phone`, `user_type`, `status`, `remark`) VALUES
(2, '13800000001', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '测试用户', '张三', '13800000001', 1, 0, '普通测试用户')
ON DUPLICATE KEY UPDATE
  `username` = '13800000001',
  `password` = '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
  `phone`    = '13800000001',
  `user_type` = 1,
  `status`   = 0,
  `del_flag` = 0;

-- ----------------------------
-- 4. 用户-角色关联
-- ----------------------------
INSERT IGNORE INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 1),   -- admin → 超级管理员
(2, 4);   -- 13800000001 → 普通用户

-- ----------------------------
-- 5. 角色-菜单关联（超级管理员拥有所有菜单权限）
-- ----------------------------
INSERT IGNORE INTO `role_menu` (`role_id`, `menu_id`)
SELECT 1, `menu_id` FROM `sys_menu`;

-- =============================================
-- 完成！登录信息汇总：
--
-- 管理端登录（adminLogin）：
--   用户名: admin
--   密码:   admin123
--
-- 用户端登录（login）：
--   账号:   13800000001
--   密码:   admin123
--
-- ⚠️ 生产环境请务必修改默认密码！
-- =============================================
