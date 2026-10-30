package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.system.annotation.Log;
import com.haikou.government.system.dto.RoleDTO;
import com.haikou.government.system.enums.BusinessType;
import com.haikou.government.system.service.SysRoleService;
import com.haikou.government.system.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-01
 */
@Tag(name = "角色管理", description = "角色增删改查及权限分配接口")
@RestController
@RequestMapping("/sysRole")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 查询角色列表
     *
     * @return 角色列表
     */
    @Operation(summary = "查询角色列表", description = "获取所有角色列表")
    @GetMapping("/list")
    public R<List<RoleVO>> list() {
        List<RoleVO> roleList = sysRoleService.getRoleList();
        return R.ok(roleList);
    }

    /**
     * 查询角色详情
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    @Operation(summary = "查询角色详情", description = "根据角色ID查询角色详细信息")
    @GetMapping("/{roleId}")
    public R<RoleVO> getById(@PathVariable Long roleId) {
        RoleVO roleVO = sysRoleService.getRoleById(roleId);
        return R.ok(roleVO);
    }

    /**
     * 新增角色
     *
     * @param roleDTO 角色参数
     * @return 是否成功
     */
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增角色", description = "添加新角色")
    @PostMapping
    public R<Boolean> add(@Valid @RequestBody RoleDTO roleDTO) {
        boolean result = sysRoleService.addRole(roleDTO);
        return R.ok(result);
    }

    /**
     * 修改角色
     *
     * @param roleDTO 角色参数
     * @return 是否成功
     */
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改角色", description = "修改角色信息")
    @PutMapping
    public R<Boolean> update(@Valid @RequestBody RoleDTO roleDTO) {
        boolean result = sysRoleService.updateRole(roleDTO);
        return R.ok(result);
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除角色", description = "删除角色")
    @DeleteMapping("/{roleId}")
    public R<Boolean> delete(@PathVariable Long roleId) {
        boolean result = sysRoleService.deleteRole(roleId);
        return R.ok(result);
    }

    /**
     * 分配菜单权限
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "分配菜单权限", description = "为角色分配菜单权限")
    @PutMapping("/{roleId}/menus")
    public R<Boolean> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        boolean result = sysRoleService.assignMenus(roleId, menuIds);
        return R.ok(result);
    }
}
