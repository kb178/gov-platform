package com.haikou.government.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.item.domain.ItemCategory;
import com.haikou.government.item.domain.ItemFavorite;
import com.haikou.government.item.domain.ItemInfo;
import com.haikou.government.item.mapper.ItemCategoryMapper;
import com.haikou.government.item.mapper.ItemFavoriteMapper;
import com.haikou.government.item.mapper.ItemInfoMapper;
import com.haikou.government.item.service.ItemFavoriteService;
import com.haikou.government.item.vo.ItemFavoriteVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 事项收藏表 服务实现类
 *
 * @author xhl
 * @since 2026-08-10
 */
@Slf4j
@Service
public class ItemFavoriteServiceImpl extends ServiceImpl<ItemFavoriteMapper, ItemFavorite> implements ItemFavoriteService {

    @Autowired
    private ItemInfoMapper itemInfoMapper;

    @Autowired
    private ItemCategoryMapper categoryMapper;

    /**
     * 收藏事项
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addFavorite(Long userId, Long itemId) {
        // 校验事项是否存在
        ItemInfo itemInfo = itemInfoMapper.selectById(itemId);
        if (itemInfo == null) {
            throw new BusinessException("事项不存在");
        }

        // 检查是否已收藏
        if (isFavorite(userId, itemId)) {
            throw new BusinessException("已收藏该事项");
        }

        ItemFavorite favorite = new ItemFavorite();
        favorite.setUserId(userId);
        favorite.setItemId(itemId);
        this.save(favorite);

        log.info("收藏事项成功: userId={}, itemId={}", userId, itemId);
        return favorite.getId();
    }

    /**
     * 取消收藏
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long userId, Long itemId) {
        boolean removed = this.remove(new LambdaQueryWrapper<ItemFavorite>()
                .eq(ItemFavorite::getUserId, userId)
                .eq(ItemFavorite::getItemId, itemId));
        if (!removed) {
            throw new BusinessException("未收藏该事项");
        }
        log.info("取消收藏成功: userId={}, itemId={}", userId, itemId);
    }

    /**
     * 查询用户是否已收藏事项
     */
    @Override
    public boolean isFavorite(Long userId, Long itemId) {
        return this.count(new LambdaQueryWrapper<ItemFavorite>()
                .eq(ItemFavorite::getUserId, userId)
                .eq(ItemFavorite::getItemId, itemId)) > 0;
    }

    /**
     * 分页查询用户的收藏列表
     */
    @Override
    public IPage<ItemFavoriteVO> getFavoritePageList(Page<ItemFavoriteVO> page, Long userId) {
        Page<ItemFavorite> favoritePage = new Page<>(page.getCurrent(), page.getSize());
        IPage<ItemFavorite> result = this.page(favoritePage, new LambdaQueryWrapper<ItemFavorite>()
                .eq(ItemFavorite::getUserId, userId)
                .orderByDesc(ItemFavorite::getCreateTime));

        Page<ItemFavoriteVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ItemFavoriteVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 实体转VO
     */
    private ItemFavoriteVO convertToVO(ItemFavorite favorite) {
        ItemFavoriteVO vo = new ItemFavoriteVO();
        vo.setId(favorite.getId());
        vo.setUserId(favorite.getUserId());
        vo.setItemId(favorite.getItemId());
        vo.setCreateTime(favorite.getCreateTime());

        // 查询事项信息
        if (favorite.getItemId() != null) {
            ItemInfo itemInfo = itemInfoMapper.selectById(favorite.getItemId());
            if (itemInfo != null) {
                vo.setItemName(itemInfo.getItemName());
                vo.setItemCode(itemInfo.getItemCode());
                vo.setSummary(itemInfo.getSummary());

                // 查询分类名称
                if (itemInfo.getCategoryId() != null) {
                    ItemCategory category = categoryMapper.selectById(itemInfo.getCategoryId());
                    if (category != null) {
                        vo.setCategoryName(category.getCategoryName());
                    }
                }
            }
        }

        return vo;
    }
}
