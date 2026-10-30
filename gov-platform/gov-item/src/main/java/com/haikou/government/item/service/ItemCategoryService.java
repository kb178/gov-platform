package com.haikou.government.item.service;

import com.haikou.government.item.domain.ItemCategory;
import com.haikou.government.item.dto.ItemCategoryDTO;
import com.haikou.government.item.vo.ItemCategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 事项分类表 服务接口
 *
 * @author xhl
 * @since 2026-08-10
 */
public interface ItemCategoryService extends IService<ItemCategory> {

    /**
     * 查询分类树形列表
     *
     * @return 树形分类列表
     */
    List<ItemCategoryVO> getCategoryTree();

    /**
     * 查询分类列表（平铺）
     *
     * @param parentId 父分类ID（可选）
     * @return 分类列表
     */
    List<ItemCategoryVO> getCategoryList(Long parentId);

    /**
     * 查询分类详情
     *
     * @param categoryId 分类ID
     * @return 分类信息
     */
    ItemCategoryVO getCategoryById(Long categoryId);

    /**
     * 新增分类
     *
     * @param dto 分类参数
     * @return 是否成功
     */
    boolean addCategory(ItemCategoryDTO dto);

    /**
     * 修改分类
     *
     * @param dto 分类参数
     * @return 是否成功
     */
    boolean updateCategory(ItemCategoryDTO dto);

    /**
     * 删除分类
     *
     * @param categoryId 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long categoryId);
}
