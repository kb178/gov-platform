package com.haikou.government.item.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haikou.government.common.core.domain.BaseEntity;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 事项表单模板表
 * </p>
 *
 * @author xhl
 * @since 2026-08-10
 */
@Getter
@Setter
@TableName("item_form_template")
public class ItemFormTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @TableId("template_id")
    private Long templateId;

    /**
     * 事项ID
     */
    @TableField("item_id")
    private Long itemId;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 表单字段配置（JSON格式）
     */
    @TableField("form_config")
    private String formConfig;

    /**
     * 状态（0草稿 1已发布）
     */
    @TableField("status")
    private Byte status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    @TableField("del_flag")
    private Byte delFlag;
}
