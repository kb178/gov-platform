package com.haikou.government.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 部门管理 DTO
 *
 * @author xhl
 * @since 2026-08-03
 */
@Schema(description = "部门管理请求参数")
@Data
public class DeptDTO {

    @Schema(description = "部门ID（修改时必填）")
    private Long deptId;

    @Schema(description = "父部门ID", example = "0")
    private Long parentId = 0L;

    @Schema(description = "部门名称", required = true, example = "技术部")
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 64, message = "部门名称不能超过64个字符")
    private String deptName;

    @Schema(description = "排序", example = "1")
    private Integer sortNum = 0;

    @Schema(description = "负责人")
    @Size(max = 64, message = "负责人不能超过64个字符")
    private String leader;

    @Schema(description = "联系电话")
    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String phone;

    @Schema(description = "邮箱")
    @Size(max = 128, message = "邮箱不能超过128个字符")
    private String email;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Byte status = 0;
}
