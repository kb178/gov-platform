package com.haikou.government.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单响应结果（树形结构）
 *
 * @author xhl
 * @since 2026-08-01
 */
@Schema(description = "菜单响应结果")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuVO {

    @Schema(description = "菜单ID", example = "1")
    private Long menuId;

    @Schema(description = "菜单名称", example = "系统管理")
    private String menuName;

    @Schema(description = "父菜单ID", example = "0")
    private Long parentId;

    @Schema(description = "排序", example = "1")
    private Integer sortNum;

    @Schema(description = "路由地址", example = "/system")
    private String path;

    @Schema(description = "组件路径", example = "system/index")
    private String component;

    @Schema(description = "菜单类型（M目录 C菜单 F按钮）", example = "M")
    private String menuType;

    @Schema(description = "权限标识", example = "system:user:list")
    private String perms;

    @Schema(description = "菜单图标", example = "setting")
    private String icon;

    @Schema(description = "是否可见（0可见 1隐藏）", example = "0")
    private Byte visible;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Byte status;

    @Schema(description = "子菜单列表")
    private List<MenuVO> children;
}
