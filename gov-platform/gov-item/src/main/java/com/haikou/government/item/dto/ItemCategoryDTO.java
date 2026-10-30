package com.haikou.government.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 事项分类 DTO
 *
 * @author xhl
 * @since 2026-08-10
 */
@Schema(description = "事项分类请求参数")
@Data
public class ItemCategoryDTO {

    @Schema(description = "分类ID（修改时必填）")
    private Long categoryId;

    @Schema(description = "父分类ID（0表示顶级分类）", example = "0")
    private Long parentId = 0L;

    @Schema(description = "分类名称", required = true, example = "户籍办理")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称不能超过64个字符")
    private String categoryName;

    @Schema(description = "分类图标", example = "icon-house")
    @Size(max = 255, message = "分类图标不能超过255个字符")
    private String icon;

    @Schema(description = "排序号", example = "0")
    private Integer sortNum = 0;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Byte status = 0;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
