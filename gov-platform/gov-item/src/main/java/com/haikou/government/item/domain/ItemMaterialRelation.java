package com.haikou.government.item.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haikou.government.common.core.domain.BaseEntity;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 事项材料关联表
 * </p>
 *
 * @author xhl
 * @since 2026-08-10
 */
@Getter
@Setter
@TableName("item_material_relation")
public class ItemMaterialRelation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId("id")
    private Long id;

    /**
     * 事项ID
     */
    @TableField("item_id")
    private Long itemId;

    /**
     * 材料ID
     */
    @TableField("material_id")
    private Long materialId;

    /**
     * 是否必须（0否 1是）
     */
    @TableField("required")
    private Byte required;

    /**
     * 排序号
     */
    @TableField("sort_num")
    private Integer sortNum;

    /**
     * 备注（针对该事项的特殊说明）
     */
    @TableField("remark")
    private String remark;
}
