package com.haikou.government.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 事项信息 DTO（请求）
 *
 * @author xhl
 * @since 2026-08-13
 */
@Data
@Schema(description = "事项信息DTO")
public class ItemInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "事项ID（修改时必填）")
    private Long itemId;

    @NotNull(message = "分类ID不能为空")
    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @Schema(description = "办理部门ID")
    private Long deptId;

    @NotBlank(message = "事项名称不能为空")
    @Size(max = 100, message = "事项名称长度不能超过100个字符")
    @Schema(description = "事项名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String itemName;

    @NotBlank(message = "事项编码不能为空")
    @Size(max = 50, message = "事项编码长度不能超过50个字符")
    @Schema(description = "事项编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String itemCode;

    @Size(max = 500, message = "简短描述长度不能超过500个字符")
    @Schema(description = "简短描述")
    private String summary;

    @Schema(description = "办理条件（富文本）")
    private String applyCondition;

    @Schema(description = "办理流程（富文本）")
    private String processFlow;

    @Size(max = 100, message = "办理时限长度不能超过100个字符")
    @Schema(description = "办理时限")
    private String processTime;

    @Size(max = 100, message = "收费标准长度不能超过100个字符")
    @Schema(description = "收费标准")
    private String feeStandard;

    @Size(max = 200, message = "办理地点长度不能超过200个字符")
    @Schema(description = "办理地点")
    private String processLocation;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "状态（0草稿 1已发布 2已下线）")
    private Byte status;

    @Schema(description = "是否支持在线办理（0否 1是）")
    private Byte supportOnline;

    @Schema(description = "排序号")
    private Integer sortNum;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注")
    private String remark;
}
