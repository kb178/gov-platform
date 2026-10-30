package com.haikou.government.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事项信息 VO（响应）
 *
 * @author xhl
 * @since 2026-08-13
 */
@Data
@Schema(description = "事项信息VO")
public class ItemInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "事项ID")
    private Long itemId;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "办理部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "事项名称")
    private String itemName;

    @Schema(description = "事项编码")
    private String itemCode;

    @Schema(description = "简短描述")
    private String summary;

    @Schema(description = "办理条件（富文本）")
    private String applyCondition;

    @Schema(description = "办理流程（富文本）")
    private String processFlow;

    @Schema(description = "办理时限")
    private String processTime;

    @Schema(description = "收费标准")
    private String feeStandard;

    @Schema(description = "办理地点")
    private String processLocation;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "状态（0草稿 1已发布 2已下线）")
    private Byte status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "是否支持在线办理（0否 1是）")
    private Byte supportOnline;

    @Schema(description = "排序号")
    private Integer sortNum;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
