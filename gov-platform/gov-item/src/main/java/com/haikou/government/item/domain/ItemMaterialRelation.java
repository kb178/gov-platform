package com.haikou.government.item.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事项材料关联表（事项与材料的多对多关系）
 *
 * 注意：不继承 BaseEntity，纯关联表只需记录关联关系本身
 *
 * @author xhl
 * @since 2026-08-10
 */
@Data
@TableName("item_material_relation")
public class ItemMaterialRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 事项ID */
    @TableField("item_id")
    private Long itemId;

    /** 材料ID */
    @TableField("material_id")
    private Long materialId;

    /** 是否必须（0否 1是） */
    @TableField("required")
    private Byte required;

    /** 排序号 */
    @TableField("sort_num")
    private Integer sortNum;

    /** 备注（针对该事项的特殊说明） */
    @TableField("remark")
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
