-- =============================================
-- 政务一体化平台 - 第三阶段建表语句
-- 审批服务模块（gov-approval）
-- 共 4 张表
-- 来源：docs/审批模块设计文档.md
-- =============================================

-- ----------------------------
-- 1. 申请单表
-- ----------------------------
CREATE TABLE `application` (
  `application_id` BIGINT NOT NULL COMMENT '申请单ID',
  `application_no` VARCHAR(32) NOT NULL COMMENT '申请编号（业务编号，如：SQ202611190001）',
  `user_id` BIGINT NOT NULL COMMENT '申请人ID（sys_user）',
  `item_id` BIGINT NOT NULL COMMENT '办理事项ID（item_info）',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0草稿 1待受理 2审核中 3退回补材料 4审批中 5已驳回 6已通过）',
  `apply_time` DATETIME DEFAULT NULL COMMENT '提交申请时间',
  `accept_time` DATETIME DEFAULT NULL COMMENT '受理时间',
  `finish_time` DATETIME DEFAULT NULL COMMENT '办结时间',
  `reject_reason` VARCHAR(500) DEFAULT '' COMMENT '驳回原因/退回原因',
  `urgency` TINYINT DEFAULT 0 COMMENT '是否加急（0普通 1加急）',
  `version` INT DEFAULT 0 COMMENT '版本号（乐观锁，防并发操作冲突）',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`application_id`),
  UNIQUE KEY `idx_application_no` (`application_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_status` (`status`),
  KEY `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB COMMENT='申请单表';

-- ----------------------------
-- 2. 申请表单数据表
-- ----------------------------
CREATE TABLE `application_form_data` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `application_id` BIGINT NOT NULL COMMENT '申请单ID',
  `template_id` BIGINT DEFAULT NULL COMMENT '使用的表单模板ID（item_form_template）',
  `form_data` JSON DEFAULT NULL COMMENT '表单数据（JSON，key=字段名，value=用户填写的值）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_application_id` (`application_id`)
) ENGINE=InnoDB COMMENT='申请表单数据表';

-- ----------------------------
-- 3. 申请材料表
-- ----------------------------
CREATE TABLE `application_material` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `application_id` BIGINT NOT NULL COMMENT '申请单ID',
  `material_id` BIGINT NOT NULL COMMENT '材料模板ID（item_material_template）',
  `material_name` VARCHAR(128) NOT NULL COMMENT '材料名称（冗余，防止模板修改影响历史）',
  `required` TINYINT DEFAULT 1 COMMENT '是否必须（0否 1是）',
  `file_url` VARCHAR(500) DEFAULT '' COMMENT '材料文件URL（上传后的地址）',
  `check_status` TINYINT DEFAULT 0 COMMENT '核验状态（0未核验 1通过 2不通过）',
  `check_remark` VARCHAR(255) DEFAULT '' COMMENT '核验备注（如：材料不清晰）',
  `check_by` VARCHAR(64) DEFAULT '' COMMENT '核验人',
  `check_time` DATETIME DEFAULT NULL COMMENT '核验时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB COMMENT='申请材料表';

-- ----------------------------
-- 4. 审批记录表
-- ----------------------------
CREATE TABLE `approval_record` (
  `record_id` BIGINT NOT NULL COMMENT '记录ID',
  `application_id` BIGINT NOT NULL COMMENT '申请单ID',
  `node_name` VARCHAR(64) NOT NULL COMMENT '审批节点名称（窗口受理/科室审核/领导审批）',
  `approver_id` BIGINT NOT NULL COMMENT '审批人ID（sys_user）',
  `approver_name` VARCHAR(64) DEFAULT '' COMMENT '审批人姓名（冗余）',
  `action` TINYINT NOT NULL COMMENT '操作（1受理 2通过 3驳回 4退回补材料 5不予受理）',
  `opinion` VARCHAR(500) DEFAULT '' COMMENT '审批意见',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_approver_id` (`approver_id`)
) ENGINE=InnoDB COMMENT='审批记录表';
