package com.haikou.government.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 字典数据 DTO
 *
 * @author xhl
 * @since 2026-08-03
 */
@Schema(description = "字典数据请求参数")
@Data
public class DictDataDTO {

    @Schema(description = "字典编码（修改时必填）")
    private Long dictCode;

    @Schema(description = "字典类型", required = true, example = "sys_user_sex")
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    @Schema(description = "字典标签", required = true, example = "男")
    @NotBlank(message = "字典标签不能为空")
    @Size(max = 100, message = "字典标签不能超过100个字符")
    private String dictLabel;

    @Schema(description = "字典值", required = true, example = "1")
    @NotBlank(message = "字典值不能为空")
    @Size(max = 100, message = "字典值不能超过100个字符")
    private String dictValue;

    @Schema(description = "排序", example = "1")
    private Integer sortNum = 0;

    @Schema(description = "样式属性")
    @Size(max = 100, message = "样式属性不能超过100个字符")
    private String cssClass;

    @Schema(description = "表格回显样式")
    @Size(max = 100, message = "表格回显样式不能超过100个字符")
    private String listClass;

    @Schema(description = "是否默认（Y是 N否）", example = "N")
    private String isDefault = "N";

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Byte status = 0;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
