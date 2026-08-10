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
 * 材料模板表（可复用）
 * </p>
 *
 * @author xhl
 * @since 2026-08-10
 */
@Getter
@Setter
@TableName("item_material_template")
public class ItemMaterialTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 材料ID
     */
    @TableId("material_id")
    private Long materialId;

    /**
     * 材料名称
     */
    @TableField("material_name")
    private String materialName;

    /**
     * 材料说明
     */
    @TableField("material_desc")
    private String materialDesc;

    /**
     * 材料类型（0纸质 1电子版 2两者都要）
     */
    @TableField("material_type")
    private Byte materialType;

    /**
     * 示例图片URL
     */
    @TableField("example_url")
    private String exampleUrl;

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
