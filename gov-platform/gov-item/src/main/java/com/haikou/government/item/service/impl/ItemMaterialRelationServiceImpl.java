package com.haikou.government.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.item.domain.ItemInfo;
import com.haikou.government.item.domain.ItemMaterialRelation;
import com.haikou.government.item.domain.ItemMaterialTemplate;
import com.haikou.government.item.dto.ItemMaterialRelationDTO;
import com.haikou.government.item.mapper.ItemInfoMapper;
import com.haikou.government.item.mapper.ItemMaterialRelationMapper;
import com.haikou.government.item.mapper.ItemMaterialTemplateMapper;
import com.haikou.government.item.service.ItemMaterialRelationService;
import com.haikou.government.item.vo.ItemMaterialRelationVO;
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
 * 事项材料关联表 服务实现类
 *
 * @author xhl
 * @since 2026-08-10
 */
@Slf4j
@Service
public class ItemMaterialRelationServiceImpl extends ServiceImpl<ItemMaterialRelationMapper, ItemMaterialRelation> implements ItemMaterialRelationService {

    @Autowired
    private ItemInfoMapper itemInfoMapper;

    @Autowired
    private ItemMaterialTemplateMapper materialTemplateMapper;

    /**
     * 查询事项的材料列表
     */
    @Override
    public List<ItemMaterialRelationVO> getMaterialsByItemId(Long itemId) {
        List<ItemMaterialRelation> list = this.list(new LambdaQueryWrapper<ItemMaterialRelation>()
                .eq(ItemMaterialRelation::getItemId, itemId)
                .orderByAsc(ItemMaterialRelation::getSortNum));

        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 批量设置事项的材料（先删后增）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setItemMaterials(ItemMaterialRelationDTO dto) {
        Long itemId = dto.getItemId();

        // 校验事项是否存在
        ItemInfo itemInfo = itemInfoMapper.selectById(itemId);
        if (itemInfo == null) {
            throw new BusinessException("事项不存在");
        }

        // 删除原有材料关联
        this.remove(new LambdaQueryWrapper<ItemMaterialRelation>()
                .eq(ItemMaterialRelation::getItemId, itemId));

        // 新增材料关联
        if (dto.getMaterials() != null && !dto.getMaterials().isEmpty()) {
            List<ItemMaterialRelation> relations = new ArrayList<>();
            int sortNum = 0;
            for (ItemMaterialRelationDTO.MaterialItem item : dto.getMaterials()) {
                // 校验材料是否存在
                ItemMaterialTemplate material = materialTemplateMapper.selectById(item.getMaterialId());
                if (material == null) {
                    throw new BusinessException("材料模板不存在: materialId=" + item.getMaterialId());
                }

                ItemMaterialRelation relation = new ItemMaterialRelation();
                relation.setItemId(itemId);
                relation.setMaterialId(item.getMaterialId());
                relation.setRequired(item.getRequired() != null ? item.getRequired() : (byte) 0);
                relation.setSortNum(item.getSortNum() != null ? item.getSortNum() : sortNum++);
                relation.setRemark(item.getRemark());
                relations.add(relation);
            }
            this.saveBatch(relations);
        }

        log.info("设置事项材料关联成功: itemId={}, 材料数量={}", itemId,
                dto.getMaterials() != null ? dto.getMaterials().size() : 0);
    }

    /**
     * 删除事项的某个材料关联
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRelation(Long id) {
        ItemMaterialRelation relation = this.getById(id);
        if (relation == null) {
            throw new BusinessException("关联记录不存在");
        }
        this.removeById(id);
        log.info("删除事项材料关联成功: id={}", id);
    }

    /**
     * 删除事项的所有材料关联
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllByItemId(Long itemId) {
        this.remove(new LambdaQueryWrapper<ItemMaterialRelation>()
                .eq(ItemMaterialRelation::getItemId, itemId));
        log.info("删除事项所有材料关联成功: itemId={}", itemId);
    }

    /**
     * 实体转VO
     */
    private ItemMaterialRelationVO convertToVO(ItemMaterialRelation relation) {
        ItemMaterialRelationVO vo = new ItemMaterialRelationVO();
        BeanUtils.copyProperties(relation, vo);

        // 查询材料模板信息
        if (relation.getMaterialId() != null) {
            ItemMaterialTemplate material = materialTemplateMapper.selectById(relation.getMaterialId());
            if (material != null) {
                vo.setMaterialName(material.getMaterialName());
                vo.setMaterialDesc(material.getMaterialDesc());
                vo.setMaterialType(material.getMaterialType());
                vo.setMaterialTypeText(getMaterialTypeText(material.getMaterialType()));
            }
        }

        // 是否必须文本
        vo.setRequiredText(relation.getRequired() != null && relation.getRequired() == 1 ? "是" : "否");

        return vo;
    }

    /**
     * 获取材料类型文本
     */
    private String getMaterialTypeText(Byte materialType) {
        if (materialType == null) {
            return "未知";
        }
        switch (materialType) {
            case 0:
                return "纸质";
            case 1:
                return "电子版";
            case 2:
                return "两者都要";
            default:
                return "未知";
        }
    }
}
