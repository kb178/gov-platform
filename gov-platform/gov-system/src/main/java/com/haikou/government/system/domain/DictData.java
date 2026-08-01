package com.haikou.government.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haikou.government.common.core.domain.BaseEntity;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 字典数据表
 * </p>
 *
 * @author xhl
 * @since 2026-08-01
 */
@Getter
@Setter
@TableName("dict_data")
public class DictData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 字典编码
     */
    @TableId("dict_code")
    private Long dictCode;

    /**
     * 字典类型
     */
    @TableField("dict_type")
    private String dictType;

    /**
     * 字典标签
     */
    @TableField("dict_label")
    private String dictLabel;

    /**
     * 字典值
     */
    @TableField("dict_value")
    private String dictValue;

    /**
     * 排序
     */
    @TableField("sort_num")
    private Integer sortNum;

    /**
     * 样式属性
     */
    @TableField("css_class")
    private String cssClass;

    /**
     * 表格回显样式
     */
    @TableField("list_class")
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    @TableField("is_default")
    private String isDefault;

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
}
