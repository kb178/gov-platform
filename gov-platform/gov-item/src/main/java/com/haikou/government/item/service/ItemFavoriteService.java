package com.haikou.government.item.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.item.domain.ItemFavorite;
import com.baomidou.mybatisplus.extension.service.IService;
import com.haikou.government.item.vo.ItemFavoriteVO;

/**
 * 事项收藏表 服务接口
 *
 * @author xhl
 * @since 2026-08-10
 */
public interface ItemFavoriteService extends IService<ItemFavorite> {

    /**
     * 收藏事项
     *
     * @param userId 用户ID
     * @param itemId 事项ID
     * @return 收藏ID
     */
    Long addFavorite(Long userId, Long itemId);

    /**
     * 取消收藏
     *
     * @param userId 用户ID
     * @param itemId 事项ID
     */
    void removeFavorite(Long userId, Long itemId);

    /**
     * 查询用户是否已收藏事项
     *
     * @param userId 用户ID
     * @param itemId 事项ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long userId, Long itemId);

    /**
     * 分页查询用户的收藏列表
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 收藏列表
     */
    IPage<ItemFavoriteVO> getFavoritePageList(Page<ItemFavoriteVO> page, Long userId);
}
