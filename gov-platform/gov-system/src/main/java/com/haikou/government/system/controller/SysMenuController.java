package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.system.dto.MenuDTO;
import com.haikou.government.system.service.SysMenuService;
import com.haikou.government.system.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-01
 */
@Tag(name = "菜单管理", description = "菜单增删改查接口")
@RestController
@RequestMapping("/sysMenu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 查询菜单列表（树形结构）
     *
     * @return 菜单树形列表
     */
    @Operation(summary = "查询菜单列表", description = "获取所有菜单，返回树形结构")
    @GetMapping("/list")
    public R<List<MenuVO>> list() {
        List<MenuVO> menuTree = sysMenuService.getMenuTree();
        return R.ok(menuTree);
    }

    /**
     * 查询菜单详情
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    @Operation(summary = "查询菜单详情", description = "根据菜单ID查询菜单详细信息")
    @GetMapping("/{menuId}")
    public R<MenuVO> getById(@PathVariable Long menuId) {
        MenuVO menuVO = sysMenuService.getMenuById(menuId);
        return R.ok(menuVO);
    }

    /**
     * 新增菜单
     *
     * @param menuDTO 菜单参数
     * @return 是否成功
     */
    @Operation(summary = "新增菜单", description = "添加新菜单（目录/菜单/按钮）")
    @PostMapping
    public R<Boolean> add(@Valid @RequestBody MenuDTO menuDTO) {
        boolean result = sysMenuService.addMenu(menuDTO);
        return R.ok(result);
    }

    /**
     * 修改菜单
     *
     * @param menuDTO 菜单参数
     * @return 是否成功
     */
    @Operation(summary = "修改菜单", description = "修改菜单信息")
    @PutMapping
    public R<Boolean> update(@Valid @RequestBody MenuDTO menuDTO) {
        boolean result = sysMenuService.updateMenu(menuDTO);
        return R.ok(result);
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 是否成功
     */
    @Operation(summary = "删除菜单", description = "删除菜单（不能删除有子菜单的菜单）")
    @DeleteMapping("/{menuId}")
    public R<Boolean> delete(@PathVariable Long menuId) {
        boolean result = sysMenuService.deleteMenu(menuId);
        return R.ok(result);
    }
}
