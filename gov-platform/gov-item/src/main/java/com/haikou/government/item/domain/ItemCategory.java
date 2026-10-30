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
 * 事项分类表
 * </p>
 *
 * @author xhl
 * @since 2026-08-10
 */
@Getter
@Setter
@TableName("item_category")
public class ItemCategory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @TableId("category_id")
    private Long categoryId;

    /**
     * 父分类ID（0表示顶级分类）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 分类名称
     */
    @TableField("category_name")
    private String categoryName;

    /**
     * 分类图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 排序号
     */
    @TableField("sort_num")
    private Integer sortNum;

    /**
     * 状态（0正常 1停用）
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
