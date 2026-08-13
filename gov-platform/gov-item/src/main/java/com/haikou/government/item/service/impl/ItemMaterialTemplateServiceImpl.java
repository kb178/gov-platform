package com.haikou.government.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.item.domain.ItemMaterialRelation;
import com.haikou.government.item.domain.ItemMaterialTemplate;
import com.haikou.government.item.dto.ItemMaterialTemplateDTO;
import com.haikou.government.item.mapper.ItemMaterialRelationMapper;
import com.haikou.government.item.mapper.ItemMaterialTemplateMapper;
import com.haikou.government.item.service.ItemMaterialTemplateService;
import com.haikou.government.item.vo.ItemMaterialTemplateVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 材料模板表（可复用） 服务实现类
 *
 * @author xhl
 * @since 2026-08-10
 */
@Slf4j
@Service
public class ItemMaterialTemplateServiceImpl extends ServiceImpl<ItemMaterialTemplateMapper, ItemMaterialTemplate> implements ItemMaterialTemplateService {

    @Autowired
    private ItemMaterialRelationMapper materialRelationMapper;

    /**
     * 分页查询材料模板列表
     */
    @Override
    public IPage<ItemMaterialTemplateVO> getMaterialPageList(Page<ItemMaterialTemplateVO> page, String materialName, Byte status) {
        LambdaQueryWrapper<ItemMaterialTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(materialName), ItemMaterialTemplate::getMaterialName, materialName)
                .eq(status != null, ItemMaterialTemplate::getStatus, status)
                .orderByDesc(ItemMaterialTemplate::getCreateTime);

        Page<ItemMaterialTemplate> materialPage = new Page<>(page.getCurrent(), page.getSize());
        IPage<ItemMaterialTemplate> result = this.page(materialPage, wrapper);

        Page<ItemMaterialTemplateVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ItemMaterialTemplateVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询材料模板详情
     */
    @Override
    public ItemMaterialTemplateVO getMaterialDetail(Long materialId) {
        ItemMaterialTemplate material = this.getById(materialId);
        if (material == null) {
            throw new BusinessException("材料模板不存在");
        }
        return convertToVO(material);
    }

    /**
     * 新增材料模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addMaterial(ItemMaterialTemplateDTO dto) {
        // 校验材料名称唯一性
        checkMaterialNameUnique(dto.getMaterialName(), null);

        ItemMaterialTemplate material = new ItemMaterialTemplate();
        BeanUtils.copyProperties(dto, material);

        // 默认状态为正常
        if (material.getStatus() == null) {
            material.setStatus((byte) 0);
        }
        // 默认材料类型为纸质
        if (material.getMaterialType() == null) {
            material.setMaterialType((byte) 0);
        }

        this.save(material);

        log.info("新增材料模板成功: materialId={}, materialName={}", material.getMaterialId(), material.getMaterialName());
        return material.getMaterialId();
    }

    /**
     * 修改材料模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMaterial(ItemMaterialTemplateDTO dto) {
        Long materialId = dto.getMaterialId();
        if (materialId == null) {
            throw new BusinessException("材料ID不能为空");
        }

        ItemMaterialTemplate existing = this.getById(materialId);
        if (existing == null) {
            throw new BusinessException("材料模板不存在");
        }

        // 校验材料名称唯一性（排除自身）
        checkMaterialNameUnique(dto.getMaterialName(), materialId);

        ItemMaterialTemplate material = new ItemMaterialTemplate();
        BeanUtils.copyProperties(dto, material);
        this.updateById(material);

        log.info("修改材料模板成功: materialId={}", materialId);
    }

    /**
     * 删除材料模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaterial(Long materialId) {
        ItemMaterialTemplate material = this.getById(materialId);
        if (material == null) {
            throw new BusinessException("材料模板不存在");
        }

        // 检查是否有关联的事项
        long relationCount = materialRelationMapper.selectCount(new LambdaQueryWrapper<ItemMaterialRelation>()
                .eq(ItemMaterialRelation::getMaterialId, materialId));
        if (relationCount > 0) {
            throw new BusinessException("该材料已被事项引用，不能删除");
        }

        this.removeById(materialId);
        log.info("删除材料模板成功: materialId={}, materialName={}", materialId, material.getMaterialName());
    }

    /**
     * 查询所有正常状态的材料模板
     */
    @Override
    public List<ItemMaterialTemplateVO> getAllActiveMaterials() {
        List<ItemMaterialTemplate> list = this.list(new LambdaQueryWrapper<ItemMaterialTemplate>()
                .eq(ItemMaterialTemplate::getStatus, 0)
                .orderByAsc(ItemMaterialTemplate::getMaterialName));
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 校验材料名称唯一性
     */
    private void checkMaterialNameUnique(String materialName, Long materialId) {
        if (!StringUtils.hasText(materialName)) {
            return;
        }
        LambdaQueryWrapper<ItemMaterialTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ItemMaterialTemplate::getMaterialName, materialName);
        if (materialId != null) {
            wrapper.ne(ItemMaterialTemplate::getMaterialId, materialId);
        }
        if (this.count(wrapper) > 0) {
            throw new BusinessException("材料名称已存在");
        }
    }

    /**
     * 实体转VO
     */
    private ItemMaterialTemplateVO convertToVO(ItemMaterialTemplate material) {
        ItemMaterialTemplateVO vo = new ItemMaterialTemplateVO();
        BeanUtils.copyProperties(material, vo);

        // 材料类型文本
        vo.setMaterialTypeText(getMaterialTypeText(material.getMaterialType()));

        // 状态文本
        vo.setStatusText(material.getStatus() != null && material.getStatus() == 0 ? "正常" : "停用");

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
