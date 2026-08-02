package com.haikou.government.system.service;

import com.haikou.government.system.domain.SysMenu;
import com.haikou.government.system.dto.MenuDTO;
import com.haikou.government.system.vo.MenuVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author xhl
 * @since 2026-08-01
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 查询菜单列表（树形结构）
     *
     * @return 菜单树形列表
     */
    List<MenuVO> getMenuTree();

    /**
     * 查询菜单详情
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    MenuVO getMenuById(Long menuId);

    /**
     * 新增菜单
     *
     * @param menuDTO 菜单参数
     * @return 是否成功
     */
    boolean addMenu(MenuDTO menuDTO);

    /**
     * 修改菜单
     *
     * @param menuDTO 菜单参数
     * @return 是否成功
     */
    boolean updateMenu(MenuDTO menuDTO);

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 是否成功
     */
    boolean deleteMenu(Long menuId);
}
