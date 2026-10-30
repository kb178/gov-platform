package com.haikou.government.item.service;

import com.haikou.government.item.domain.ItemMaterialRelation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.haikou.government.item.dto.ItemMaterialRelationDTO;
import com.haikou.government.item.vo.ItemMaterialRelationVO;

import java.util.List;

/**
 * 事项材料关联表 服务接口
 *
 * @author xhl
 * @since 2026-08-10
 */
public interface ItemMaterialRelationService extends IService<ItemMaterialRelation> {

    /**
     * 查询事项的材料列表
     *
     * @param itemId 事项ID
     * @return 材料关联列表
     */
    List<ItemMaterialRelationVO> getMaterialsByItemId(Long itemId);

    /**
     * 批量设置事项的材料
     *
     * @param dto 材料关联信息
     */
    void setItemMaterials(ItemMaterialRelationDTO dto);

    /**
     * 删除事项的某个材料关联
     *
     * @param id 关联ID
     */
    void deleteRelation(Long id);

    /**
     * 删除事项的所有材料关联
     *
     * @param itemId 事项ID
     */
    void deleteAllByItemId(Long itemId);
}
