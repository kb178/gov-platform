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
 * 事项信息表
 * </p>
 *
 * @author xhl
 * @since 2026-08-10
 */
@Getter
@Setter
@TableName("item_info")
public class ItemInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 事项ID
     */
    @TableId("item_id")
    private Long itemId;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 办理部门ID（关联sys_dept）
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 事项名称
     */
    @TableField("item_name")
    private String itemName;

    /**
     * 事项编码（唯一标识）
     */
    @TableField("item_code")
    private String itemCode;

    /**
     * 简短描述（列表页显示）
     */
    @TableField("summary")
    private String summary;

    /**
     * 办理条件（富文本）
     */
    @TableField("apply_condition")
    private String applyCondition;

    /**
     * 办理流程（富文本）
     */
    @TableField("process_flow")
    private String processFlow;

    /**
     * 办理时限（如：10个工作日）
     */
    @TableField("process_time")
    private String processTime;

    /**
     * 收费标准（如：免费/20元）
     */
    @TableField("fee_standard")
    private String feeStandard;

    /**
     * 办理地点
     */
    @TableField("process_location")
    private String processLocation;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 状态（0草稿 1已发布 2已下线）
     */
    @TableField("status")
    private Byte status;

    /**
     * 是否支持在线办理（0否 1是）
     */
    @TableField("support_online")
    private Byte supportOnline;

    /**
     * 排序号
     */
    @TableField("sort_num")
    private Integer sortNum;

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
