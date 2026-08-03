package com.haikou.government.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 角色管理 DTO
 *
 * @author xhl
 * @since 2026-08-03
 */
@Schema(description = "角色管理请求参数")
@Data
public class RoleDTO {

    @Schema(description = "角色ID（修改时必填）")
    private Long roleId;

    @Schema(description = "角色名称", required = true, example = "管理员")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称不能超过64个字符")
    private String roleName;

    @Schema(description = "角色标识", required = true, example = "admin")
    @NotBlank(message = "角色标识不能为空")
    @Size(max = 100, message = "角色标识不能超过100个字符")
    private String roleKey;

    @Schema(description = "排序", example = "1")
    private Integer sortNum = 0;

    @Schema(description = "数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）", example = "1")
    private Byte dataScope = 1;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Byte status = 0;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    @Schema(description = "菜单ID列表（用于分配菜单权限）")
    private List<Long> menuIds;
}
