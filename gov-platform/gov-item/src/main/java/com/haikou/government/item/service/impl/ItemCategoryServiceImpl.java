package com.haikou.government.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.common.redis.utils.RedisUtils;
import com.haikou.government.item.domain.ItemCategory;
import com.haikou.government.item.domain.ItemInfo;
import com.haikou.government.item.dto.ItemCategoryDTO;
import com.haikou.government.item.mapper.ItemCategoryMapper;
import com.haikou.government.item.mapper.ItemInfoMapper;
import com.haikou.government.item.service.ItemCategoryService;
import com.haikou.government.item.vo.ItemCategoryVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 事项分类表 服务实现类
 *
 * @author xhl
 * @since 2026-08-10
 */
@Slf4j
@Service
public class ItemCategoryServiceImpl extends ServiceImpl<ItemCategoryMapper, ItemCategory> implements ItemCategoryService {

    @Autowired
    private ItemInfoMapper itemInfoMapper;

    @Autowired
    private RedisUtils redisUtils;

    /** 分类树缓存 key */
    private static final String CATEGORY_TREE_CACHE_KEY = "item:category:tree";

    /** 分类树缓存有效期（秒）— 30分钟 */
    private static final long CATEGORY_TREE_CACHE_TTL = 1800;

    /**
     * 查询分类树形列表（带缓存）
     *
     * 缓存策略：读多写少，分类变动少但查询频繁
     * - 查询时先查 Redis，命中直接返回
     * - 未命中查库，结果写入 Redis（30分钟过期）
     * - 新增/修改/删除分类时清除缓存
     */
    @Override
    public List<ItemCategoryVO> getCategoryTree() {
        // 1. 先查缓存
        List<ItemCategoryVO> cached = redisUtils.get(CATEGORY_TREE_CACHE_KEY);
        if (cached != null) {
            log.debug("分类树命中缓存");
            return cached;
        }

        // 2. 缓存未命中，查数据库
        List<ItemCategory> allCategories = this.list(new LambdaQueryWrapper<ItemCategory>()
                .orderByAsc(ItemCategory::getSortNum));

        // 3. 转换为VO列表并构建树形结构
        List<ItemCategoryVO> allVOs = allCategories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        List<ItemCategoryVO> tree = buildTree(allVOs, 0L);

        // 4. 写入缓存
        redisUtils.set(CATEGORY_TREE_CACHE_KEY, tree, CATEGORY_TREE_CACHE_TTL);
        log.debug("分类树已写入缓存");

        return tree;
    }

    /**
     * 查询分类列表（平铺）
     */
    @Override
    public List<ItemCategoryVO> getCategoryList(Long parentId) {
        LambdaQueryWrapper<ItemCategory> wrapper = new LambdaQueryWrapper<>();
        if (parentId != null) {
            wrapper.eq(ItemCategory::getParentId, parentId);
        }
        wrapper.orderByAsc(ItemCategory::getSortNum);

        List<ItemCategory> list = this.list(wrapper);
        return list.stream()
                .map(category -> {
                    ItemCategoryVO vo = convertToVO(category);
                    // 统计该分类下的事项数量
                    Long itemCount = itemInfoMapper.selectCount(new LambdaQueryWrapper<ItemInfo>()
                            .eq(ItemInfo::getCategoryId, category.getCategoryId())
                            .eq(ItemInfo::getStatus, (byte) 1)); // 只统计已发布的
                    vo.setItemCount(itemCount.intValue());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 查询分类详情
     */
    @Override
    public ItemCategoryVO getCategoryById(Long categoryId) {
        ItemCategory category = this.getById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return convertToVO(category);
    }

    /**
     * 新增分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCategory(ItemCategoryDTO dto) {
        // 校验分类名称唯一性（同一父分类下）
        checkCategoryNameUnique(dto.getParentId(), dto.getCategoryName(), null);

        ItemCategory category = new ItemCategory();
        BeanUtils.copyProperties(dto, category);
        this.save(category);

        // 清除分类树缓存
        redisUtils.delete(CATEGORY_TREE_CACHE_KEY);

        log.info("新增事项分类成功: categoryId={}, categoryName={}", category.getCategoryId(), category.getCategoryName());
        return true;
    }

    /**
     * 修改分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCategory(ItemCategoryDTO dto) {
        Long categoryId = dto.getCategoryId();
        if (categoryId == null) {
            throw new BusinessException("分类ID不能为空");
        }

        ItemCategory existing = this.getById(categoryId);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }

        // 校验分类名称唯一性（同一父分类下，排除自身）
        checkCategoryNameUnique(dto.getParentId(), dto.getCategoryName(), categoryId);

        // 不能将自己设为自己的子分类
        if (categoryId.equals(dto.getParentId())) {
            throw new BusinessException("父分类不能是自己");
        }

        ItemCategory category = new ItemCategory();
        BeanUtils.copyProperties(dto, category);
        this.updateById(category);

        // 清除分类树缓存
        redisUtils.delete(CATEGORY_TREE_CACHE_KEY);

        log.info("修改事项分类成功: categoryId={}", categoryId);
        return true;
    }

    /**
     * 删除分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long categoryId) {
        ItemCategory category = this.getById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否有子分类
        long childCount = this.count(new LambdaQueryWrapper<ItemCategory>()
                .eq(ItemCategory::getParentId, categoryId));
        if (childCount > 0) {
            throw new BusinessException("该分类下有子分类，不能删除");
        }

        // 检查是否有关联的事项
        long itemCount = itemInfoMapper.selectCount(new LambdaQueryWrapper<ItemInfo>()
                .eq(ItemInfo::getCategoryId, categoryId));
        if (itemCount > 0) {
            throw new BusinessException("该分类下有事项，不能删除");
        }

        // 删除分类
        this.removeById(categoryId);

        // 清除分类树缓存
        redisUtils.delete(CATEGORY_TREE_CACHE_KEY);

        log.info("删除事项分类成功: categoryId={}, categoryName={}", categoryId, category.getCategoryName());
        return true;
    }

    /**
     * 校验分类名称唯一性（同一父分类下）
     */
    private void checkCategoryNameUnique(Long parentId, String categoryName, Long categoryId) {
        LambdaQueryWrapper<ItemCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ItemCategory::getParentId, parentId)
                .eq(ItemCategory::getCategoryName, categoryName);
        if (categoryId != null) {
            wrapper.ne(ItemCategory::getCategoryId, categoryId);
        }
        if (this.count(wrapper) > 0) {
            throw new BusinessException("同一层级下分类名称已存在");
        }
    }

    /**
     * 构建树形结构
     */
    private List<ItemCategoryVO> buildTree(List<ItemCategoryVO> allVOs, Long parentId) {
        List<ItemCategoryVO> tree = new ArrayList<>();
        for (ItemCategoryVO vo : allVOs) {
            //1、第一次parentId = 0，判断出一级节点（父节点）
            //2、递归进来后会根据一级节点的id，和所有节点的getParentId属性对比如果相等说明，是它的孩子
            if (parentId.equals(vo.getParentId())) {
                // 递归查找子分类
                List<ItemCategoryVO> children = buildTree(allVOs, vo.getCategoryId());
                vo.setChildren(children);
                tree.add(vo);
            }
        }
        return tree;
    }

    /**
     * 实体转VO
     */
    private ItemCategoryVO convertToVO(ItemCategory category) {
        ItemCategoryVO vo = new ItemCategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
