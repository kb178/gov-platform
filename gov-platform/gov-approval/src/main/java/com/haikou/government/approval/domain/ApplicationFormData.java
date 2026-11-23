package com.haikou.government.approval.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 申请表单数据表
 *
 * 用户填写的表单数据，存 JSON 格式（因为每个事项的表单字段不同）
 * 不继承 BaseEntity：这个表只有创建/更新时间，不需要审计字段
 *
 * @author xhl
 * @since 2026-11-23
 */
@Data
@TableName("application_form_data")
public class ApplicationFormData implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 申请单ID */
    @TableField("application_id")
    private Long applicationId;

    /** 使用的表单模板ID（item_form_template） */
    @TableField("template_id")
    private Long templateId;

    /** 表单数据（JSON格式，key=字段名，value=用户填写的值） */
    @TableField("form_data")
    private String formData;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
