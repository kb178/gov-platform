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
 * 申请材料表
 *
 * 用户提交的材料清单，对照 item_material_relation 中配置的必需材料
 * 不继承 BaseEntity：材料名称已冗余存储，不需要审计字段
 *
 * @author xhl
 * @since 2026-11-23
 */
@Data
@TableName("application_material")
public class ApplicationMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 申请单ID */
    @TableField("application_id")
    private Long applicationId;

    /** 材料模板ID（item_material_template） */
    @TableField("material_id")
    private Long materialId;

    /** 材料名称（冗余存储，防止模板改名影响历史记录） */
    @TableField("material_name")
    private String materialName;

    /** 是否必须（0否 1是） */
    @TableField("required")
    private Byte required;

    /** 材料文件URL（上传后的地址） */
    @TableField("file_url")
    private String fileUrl;

    /** 核验状态（0未核验 1通过 2不通过） */
    @TableField("check_status")
    private Byte checkStatus;

    /** 核验备注（如：材料不清晰） */
    @TableField("check_remark")
    private String checkRemark;

    /** 核验人 */
    @TableField("check_by")
    private String checkBy;

    /** 核验时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("check_time")
    private LocalDateTime checkTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
