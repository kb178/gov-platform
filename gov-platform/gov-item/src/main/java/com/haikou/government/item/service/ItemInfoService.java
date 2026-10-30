package com.haikou.government.item.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.item.domain.ItemInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.haikou.government.item.dto.ItemInfoDTO;
import com.haikou.government.item.vo.ItemInfoVO;

/**
 * 事项信息表 服务接口
 *
 * @author xhl
 * @since 2026-08-10
 */
public interface ItemInfoService extends IService<ItemInfo> {

    /**
     * 分页查询事项列表
     *
     * @param page       分页参数
     * @param categoryId 分类ID（可选）
     * @param itemName   事项名称（可选，模糊查询）
     * @param status     状态（可选）
     * @return 事项分页列表
     */
    IPage<ItemInfoVO> getItemPageList(Page<ItemInfoVO> page, Long categoryId, String itemName, Byte status);

    /**
     * 查询事项详情
     *
     * @param itemId 事项ID
     * @return 事项详情
     */
    ItemInfoVO getItemDetail(Long itemId);

    /**
     * 新增事项
     *
     * @param dto 事项信息
     * @return 事项ID
     */
    Long addItem(ItemInfoDTO dto);

    /**
     * 修改事项
     *
     * @param dto 事项信息
     */
    void updateItem(ItemInfoDTO dto);

    /**
     * 删除事项
     *
     * @param itemId 事项ID
     */
    void deleteItem(Long itemId);

    /**
     * 发布事项
     *
     * @param itemId 事项ID
     */
    void publishItem(Long itemId);

    /**
     * 下线事项
     *
     * @param itemId 事项ID
     */
    void offlineItem(Long itemId);
}
