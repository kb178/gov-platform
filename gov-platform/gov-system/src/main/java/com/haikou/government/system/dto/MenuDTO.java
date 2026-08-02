package com.haikou.government.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 菜单请求参数（新增/修改）
 *
 * @author xhl
 * @since 2026-08-01
 */
@Schema(description = "菜单请求参数")
@Data
public class MenuDTO {

    @Schema(description = "菜单ID（修改时必填）", example = "1")
    private Long menuId;

    @Schema(description = "菜单名称", required = true, example = "用户管理")
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 64, message = "菜单名称长度不能超过64个字符")
    private String menuName;

    @Schema(description = "父菜单ID（0表示顶级）", example = "0")
    private Long parentId;

    @Schema(description = "排序", example = "1")
    private Integer sortNum;

    @Schema(description = "路由地址", example = "/user")
    @Size(max = 200, message = "路由地址长度不能超过200个字符")
    private String path;

    @Schema(description = "组件路径", example = "system/user/index")
    @Size(max = 255, message = "组件路径长度不能超过255个字符")
    private String component;

    @Schema(description = "菜单类型（M目录 C菜单 F按钮）", required = true, example = "C")
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    @Schema(description = "权限标识", example = "system:user:list")
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String perms;

    @Schema(description = "菜单图标", example = "user")
    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
    private String icon;

    @Schema(description = "是否可见（0可见 1隐藏）", example = "0")
    private Byte visible;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Byte status;
}
