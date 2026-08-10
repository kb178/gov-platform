-- =============================================
-- 政务一体化平台 - 第二阶段建表语句
-- 事项服务模块（gov-item）
-- 共 6 张表
-- =============================================

-- ----------------------------
-- 1. 事项分类表
-- ----------------------------
CREATE TABLE `item_category` (
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID（0表示顶级分类）',
  `category_name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(255) DEFAULT '' COMMENT '分类图标',
  `sort_num` INT DEFAULT 0 COMMENT '排序号',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`category_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB COMMENT='事项分类表';

-- ----------------------------
-- 2. 事项信息表
-- ----------------------------
CREATE TABLE `item_info` (
  `item_id` BIGINT NOT NULL COMMENT '事项ID',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `dept_id` BIGINT DEFAULT NULL COMMENT '办理部门ID（关联sys_dept）',
  `item_name` VARCHAR(128) NOT NULL COMMENT '事项名称',
  `item_code` VARCHAR(64) NOT NULL COMMENT '事项编码（唯一标识）',
  `summary` VARCHAR(500) DEFAULT '' COMMENT '简短描述（列表页显示）',
  `apply_condition` TEXT COMMENT '办理条件（富文本）',
  `process_flow` TEXT COMMENT '办理流程（富文本）',
  `process_time` VARCHAR(64) DEFAULT '' COMMENT '办理时限（如：10个工作日）',
  `fee_standard` VARCHAR(255) DEFAULT '' COMMENT '收费标准（如：免费/20元）',
  `process_location` VARCHAR(255) DEFAULT '' COMMENT '办理地点',
  `contact_phone` VARCHAR(20) DEFAULT '' COMMENT '联系电话',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0草稿 1已发布 2已下线）',
  `support_online` TINYINT DEFAULT 1 COMMENT '是否支持在线办理（0否 1是）',
  `sort_num` INT DEFAULT 0 COMMENT '排序号',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `idx_item_code` (`item_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='事项信息表';

-- ----------------------------
-- 3. 材料模板表（可复用的材料定义）
-- ----------------------------
CREATE TABLE `item_material_template` (
  `material_id` BIGINT NOT NULL COMMENT '材料ID',
  `material_name` VARCHAR(128) NOT NULL COMMENT '材料名称',
  `material_desc` VARCHAR(500) DEFAULT '' COMMENT '材料说明',
  `material_type` TINYINT DEFAULT 0 COMMENT '材料类型（0纸质 1电子版 2两者都要）',
  `example_url` VARCHAR(500) DEFAULT '' COMMENT '示例图片URL',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`material_id`)
) ENGINE=InnoDB COMMENT='材料模板表（可复用）';

-- ----------------------------
-- 4. 事项材料关联表（事项与材料的多对多关系）
-- ----------------------------
CREATE TABLE `item_material_relation` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `item_id` BIGINT NOT NULL COMMENT '事项ID',
  `material_id` BIGINT NOT NULL COMMENT '材料ID',
  `required` TINYINT DEFAULT 1 COMMENT '是否必须（0否 1是）',
  `sort_num` INT DEFAULT 0 COMMENT '排序号',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注（针对该事项的特殊说明）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_item_material` (`item_id`, `material_id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB COMMENT='事项材料关联表';

-- ----------------------------
-- 5. 表单模板表
-- ----------------------------
CREATE TABLE `item_form_template` (
  `template_id` BIGINT NOT NULL COMMENT '模板ID',
  `item_id` BIGINT NOT NULL COMMENT '事项ID',
  `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
  `form_config` JSON COMMENT '表单字段配置（JSON格式）',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0草稿 1已发布）',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`template_id`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB COMMENT='事项表单模板表';

-- ----------------------------
-- 6. 事项收藏表
-- ----------------------------
CREATE TABLE `item_favorite` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `item_id` BIGINT NOT NULL COMMENT '事项ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_item` (`user_id`, `item_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='事项收藏表';

-- =============================================
-- 测试数据
-- =============================================

-- ----------------------------
-- 事项分类测试数据
-- ----------------------------
INSERT INTO `item_category` (`category_id`, `parent_id`, `category_name`, `icon`, `sort_num`, `status`) VALUES
(1, 0, '户籍办理', 'icon-house', 1, 0),
(2, 0, '社保服务', 'icon-social', 2, 0),
(3, 0, '工商注册', 'icon-business', 3, 0),
(4, 0, '公积金', 'icon-fund', 4, 0),
(5, 0, '教育服务', 'icon-education', 5, 0),
(6, 1, '身份证办理', 'icon-idcard', 1, 0),
(7, 1, '户口迁移', 'icon-move', 2, 0),
(8, 1, '居住证办理', 'icon-residence', 3, 0),
(9, 2, '社保查询', 'icon-search', 1, 0),
(10, 2, '医保报销', 'icon-medical', 2, 0),
(11, 3, '营业执照办理', 'icon-license', 1, 0),
(12, 3, '个体工商户注册', 'icon-shop', 2, 0);

-- ----------------------------
-- 事项信息测试数据（包含dept_id）
-- ----------------------------
INSERT INTO `item_info` (`item_id`, `category_id`, `dept_id`, `item_name`, `item_code`, `summary`, `apply_condition`, `process_flow`, `process_time`, `fee_standard`, `process_location`, `contact_phone`, `status`, `support_online`) VALUES
(1, 6, 101, '身份证首次申领', 'IDCARD_FIRST', '年满16周岁公民首次申领身份证', '1. 年满16周岁的本市户籍公民\n2. 未满16周岁由监护人代为申请', '1. 网上预约\n2. 现场拍照\n3. 采集指纹\n4. 缴费\n5. 领取证件', '10个工作日', '首次申领免费', '各区公安分局户籍窗口', '0898-12345', 1, 1),
(2, 6, 101, '身份证补办', 'IDCARD_REPAIR', '身份证丢失或损坏后补办', '1. 本市户籍公民\n2. 身份证丢失或损坏', '1. 网上预约\n2. 现场办理\n3. 缴费\n4. 领取证件', '10个工作日', '补办收费20元', '各区公安分局户籍窗口', '0898-12345', 1, 1),
(3, 7, 101, '户口迁入', 'HUKOU_MOVE_IN', '外地户口迁入本市', '1. 有合法稳定住所\n2. 有合法稳定就业\n3. 符合本市落户条件', '1. 提交申请\n2. 材料审核\n3. 审批通过\n4. 办理迁入', '15个工作日', '免费', '各区公安分局户籍窗口', '0898-12345', 1, 1),
(4, 8, 102, '居住证首次申领', 'RESIDENCE_FIRST', '非本市户籍人员申领居住证', '1. 在本市居住满半年\n2. 有合法稳定住所或就业', '1. 网上预约\n2. 现场办理\n3. 领取证件', '15个工作日', '免费', '各社区服务中心', '0898-12345', 1, 1),
(5, 9, 103, '社保缴费查询', 'SOCIAL_QUERY', '查询个人社保缴费记录', '1. 已参加社会保险', '1. 登录系统\n2. 选择查询类型\n3. 查看结果', '即时', '免费', '网上办理', '0898-12333', 1, 1),
(6, 10, 103, '医保报销申请', 'MEDICAL_REIMBURSE', '申请医疗费用报销', '1. 已参加医疗保险\n2. 在定点医疗机构就医', '1. 提交报销申请\n2. 材料审核\n3. 费用核算\n4. 打款到银行卡', '20个工作日', '按政策比例报销', '各区社保经办机构', '0898-12333', 1, 1),
(7, 11, 104, '营业执照新办', 'BUSINESS_LICENSE', '企业首次申请营业执照', '1. 有合法经营场所\n2. 有符合规定的公司名称\n3. 有股东和出资', '1. 名称预核准\n2. 提交申请材料\n3. 材料审核\n4. 领取营业执照', '5个工作日', '免费', '各区市场监督管理局', '0898-12315', 1, 1),
(8, 12, 104, '个体工商户注册', 'INDIVIDUAL_BUSINESS', '个体工商户设立登记', '1. 有经营能力\n2. 有合法经营场所', '1. 名称预核准\n2. 提交申请材料\n3. 材料审核\n4. 领取营业执照', '3个工作日', '免费', '各区市场监督管理局', '0898-12315', 1, 1);

-- ----------------------------
-- 材料模板测试数据（可复用）
-- ----------------------------
INSERT INTO `item_material_template` (`material_id`, `material_name`, `material_desc`, `material_type`, `status`) VALUES
(1, '身份证原件', '申请人有效身份证，正反面', 0, 0),
(2, '户口簿原件', '用于核实户籍信息', 0, 0),
(3, '近期免冠照片', '白底彩色照片，尺寸33mm×48mm', 1, 0),
(4, '房产证或租赁合同', '在本市的住所证明', 0, 0),
(5, '就业证明', '劳动合同或营业执照', 0, 0),
(6, '医保卡', '社会保障卡', 0, 0),
(7, '医疗费用发票', '医院开具的正式发票', 0, 0),
(8, '费用明细清单', '医院提供的费用明细', 0, 0),
(9, '诊断证明', '医院出具的诊断证明', 0, 0),
(10, '公司名称预先核准通知书', '工商局核发', 0, 0),
(11, '公司章程', '全体股东签字', 0, 0),
(12, '股东身份证明', '自然人提供身份证，法人提供营业执照', 0, 0),
(13, '经营场所证明', '房产证或租赁合同', 0, 0),
(14, '法定代表人任职文件', '股东会决议或董事会决议', 0, 0),
(15, '个体工商户登记申请书', '工商局统一格式', 0, 0);

-- ----------------------------
-- 事项材料关联测试数据
-- ----------------------------
INSERT INTO `item_material_relation` (`id`, `item_id`, `material_id`, `required`, `sort_num`) VALUES
-- 身份证首次申领
(1, 1, 2, 1, 1),   -- 户口簿原件（必须）
(2, 1, 3, 1, 2),   -- 近期免冠照片（必须）
-- 身份证补办
(3, 2, 2, 1, 1),   -- 户口簿原件（必须）
(4, 2, 3, 1, 2),   -- 近期免冠照片（必须）
-- 户口迁入
(5, 3, 1, 1, 1),   -- 身份证原件（必须）
(6, 3, 2, 1, 2),   -- 户口簿原件（必须）
(7, 3, 4, 1, 3),   -- 房产证或租赁合同（必须）
(8, 3, 5, 1, 4),   -- 就业证明（必须）
-- 居住证首次申领
(9, 4, 1, 1, 1),    -- 身份证原件（必须）
(10, 4, 3, 1, 2),   -- 近期免冠照片（必须）
(11, 4, 4, 1, 3),   -- 房产证或租赁合同（必须）
(12, 4, 5, 1, 4),   -- 就业证明（必须）
-- 医保报销申请
(13, 6, 1, 1, 1),   -- 身份证原件（必须）
(14, 6, 6, 1, 2),   -- 医保卡（必须）
(15, 6, 7, 1, 3),   -- 医疗费用发票（必须）
(16, 6, 8, 1, 4),   -- 费用明细清单（必须）
(17, 6, 9, 1, 5),   -- 诊断证明（必须）
-- 营业执照新办
(18, 7, 10, 1, 1),  -- 公司名称预先核准通知书（必须）
(19, 7, 11, 1, 2),  -- 公司章程（必须）
(20, 7, 12, 1, 3),  -- 股东身份证明（必须）
(21, 7, 13, 1, 4),  -- 经营场所证明（必须）
(22, 7, 14, 1, 5),  -- 法定代表人任职文件（必须）
-- 个体工商户注册
(23, 8, 1, 1, 1),   -- 身份证原件（必须）
(24, 8, 13, 1, 2),  -- 经营场所证明（必须）
(25, 8, 15, 1, 3);  -- 个体工商户登记申请书（必须）

-- ----------------------------
-- 表单模板测试数据（JSON格式）
-- ----------------------------
INSERT INTO `item_form_template` (`template_id`, `item_id`, `template_name`, `form_config`, `status`) VALUES
(1, 1, '身份证首次申领表单', '{
  "fields": [
    {"name": "姓名", "type": "input", "required": true, "placeholder": "请输入姓名"},
    {"name": "性别", "type": "radio", "options": ["男", "女"], "required": true},
    {"name": "民族", "type": "select", "options": ["汉族", "回族", "藏族", "其他"], "required": true},
    {"name": "出生日期", "type": "date", "required": true},
    {"name": "身份证号", "type": "input", "required": true, "validator": "idCard"},
    {"name": "联系电话", "type": "input", "required": true, "validator": "phone"},
    {"name": "户籍地址", "type": "input", "required": true},
    {"name": "现住地址", "type": "input", "required": true},
    {"name": "申请原因", "type": "select", "options": ["首次申领", "迁入换领"], "required": true},
    {"name": "照片上传", "type": "upload", "accept": "image/*", "maxCount": 1, "required": true}
  ]
}', 1),
(2, 7, '营业执照新办表单', '{
  "fields": [
    {"name": "公司名称", "type": "input", "required": true, "placeholder": "请输入公司全称"},
    {"name": "统一社会信用代码", "type": "input", "required": false},
    {"name": "公司类型", "type": "select", "options": ["有限责任公司", "股份有限公司", "个人独资企业"], "required": true},
    {"name": "法定代表人", "type": "input", "required": true},
    {"name": "注册资本", "type": "number", "required": true, "suffix": "万元"},
    {"name": "经营范围", "type": "textarea", "required": true, "placeholder": "请详细描述经营范围"},
    {"name": "经营场所", "type": "input", "required": true},
    {"name": "营业期限", "type": "daterange", "required": true},
    {"name": "联系电话", "type": "input", "required": true, "validator": "phone"},
    {"name": "材料上传", "type": "upload", "accept": "image/*,.pdf", "maxCount": 10, "required": true}
  ]
}', 1);

-- ----------------------------
-- 事项收藏测试数据
-- ----------------------------
INSERT INTO `item_favorite` (`id`, `user_id`, `item_id`) VALUES
(1, 1, 1),  -- 用户1收藏了身份证首次申领
(2, 1, 5),  -- 用户1收藏了社保缴费查询
(3, 2, 7);  -- 用户2收藏了营业执照新办
