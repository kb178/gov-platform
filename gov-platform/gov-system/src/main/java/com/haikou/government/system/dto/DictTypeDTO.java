package com.haikou.government.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 字典类型 DTO
 *
 * @author xhl
 * @since 2026-08-03
 */
@Schema(description = "字典类型请求参数")
@Data
public class DictTypeDTO {

    @Schema(description = "字典类型ID（修改时必填）")
    private Long dictId;

    @Schema(description = "字典名称", required = true, example = "性别")
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称不能超过100个字符")
    private String dictName;

    @Schema(description = "字典类型", required = true, example = "sys_user_sex")
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型不能超过100个字符")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "字典类型只能包含小写字母、数字和下划线，且以字母开头")
    private String dictType;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Byte status = 0;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
