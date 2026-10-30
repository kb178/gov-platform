package com.haikou.government.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.item.domain.ItemFormTemplate;
import com.haikou.government.item.domain.ItemInfo;
import com.haikou.government.item.dto.ItemFormTemplateDTO;
import com.haikou.government.item.mapper.ItemFormTemplateMapper;
import com.haikou.government.item.mapper.ItemInfoMapper;
import com.haikou.government.item.service.ItemFormTemplateService;
import com.haikou.government.item.vo.ItemFormTemplateVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 事项表单模板表 服务实现类
 *
 * @author xhl
 * @since 2026-08-10
 */
@Slf4j
@Service
public class ItemFormTemplateServiceImpl extends ServiceImpl<ItemFormTemplateMapper, ItemFormTemplate> implements ItemFormTemplateService {

    @Autowired
    private ItemInfoMapper itemInfoMapper;

    /**
     * 查询事项的表单模板列表
     */
    @Override
    public List<ItemFormTemplateVO> getTemplatesByItemId(Long itemId) {
        List<ItemFormTemplate> list = this.list(new LambdaQueryWrapper<ItemFormTemplate>()
                .eq(ItemFormTemplate::getItemId, itemId)
                .orderByDesc(ItemFormTemplate::getCreateTime));
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询模板详情
     */
    @Override
    public ItemFormTemplateVO getTemplateDetail(Long templateId) {
        ItemFormTemplate template = this.getById(templateId);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        return convertToVO(template);
    }

    /**
     * 新增模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTemplate(ItemFormTemplateDTO dto) {
        // 校验事项是否存在
        ItemInfo itemInfo = itemInfoMapper.selectById(dto.getItemId());
        if (itemInfo == null) {
            throw new BusinessException("事项不存在");
        }

        ItemFormTemplate template = new ItemFormTemplate();
        BeanUtils.copyProperties(dto, template);

        // 默认状态为草稿
        if (template.getStatus() == null) {
            template.setStatus((byte) 0);
        }

        this.save(template);

        log.info("新增表单模板成功: templateId={}, templateName={}", template.getTemplateId(), template.getTemplateName());
        return template.getTemplateId();
    }

    /**
     * 修改模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(ItemFormTemplateDTO dto) {
        Long templateId = dto.getTemplateId();
        if (templateId == null) {
            throw new BusinessException("模板ID不能为空");
        }

        ItemFormTemplate existing = this.getById(templateId);
        if (existing == null) {
            throw new BusinessException("模板不存在");
        }

        // 已发布的模板不能修改（需先禁用）
        if (existing.getStatus() != null && existing.getStatus() == 1) {
            throw new BusinessException("已启用的模板不能修改，请先禁用");
        }

        ItemFormTemplate template = new ItemFormTemplate();
        BeanUtils.copyProperties(dto, template);
        this.updateById(template);

        log.info("修改表单模板成功: templateId={}", templateId);
    }

    /**
     * 删除模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long templateId) {
        ItemFormTemplate template = this.getById(templateId);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        // 已启用的模板不能删除
        if (template.getStatus() != null && template.getStatus() == 1) {
            throw new BusinessException("已启用的模板不能删除，请先禁用");
        }

        this.removeById(templateId);
        log.info("删除表单模板成功: templateId={}, templateName={}", templateId, template.getTemplateName());
    }

    /**
     * 启用模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableTemplate(Long templateId) {
        ItemFormTemplate template = this.getById(templateId);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        // 先禁用同一事项下的其他模板（一个事项只能有一个启用的模板）
        disableOtherTemplates(template.getItemId(), templateId);

        // 启用当前模板
        ItemFormTemplate update = new ItemFormTemplate();
        update.setTemplateId(templateId);
        update.setStatus((byte) 1);
        this.updateById(update);

        log.info("启用表单模板成功: templateId={}, templateName={}", templateId, template.getTemplateName());
    }

    /**
     * 禁用模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableTemplate(Long templateId) {
        ItemFormTemplate template = this.getById(templateId);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        ItemFormTemplate update = new ItemFormTemplate();
        update.setTemplateId(templateId);
        update.setStatus((byte) 0);
        this.updateById(update);

        log.info("禁用表单模板成功: templateId={}, templateName={}", templateId, template.getTemplateName());
    }

    /**
     * 获取事项当前启用的模板
     */
    @Override
    public ItemFormTemplateVO getActiveTemplate(Long itemId) {
        ItemFormTemplate template = this.getOne(new LambdaQueryWrapper<ItemFormTemplate>()
                .eq(ItemFormTemplate::getItemId, itemId)
                .eq(ItemFormTemplate::getStatus, 1)
                .last("LIMIT 1"));
        return template != null ? convertToVO(template) : null;
    }

    /**
     * 禁用同一事项下的其他模板
     */
    private void disableOtherTemplates(Long itemId, Long excludeTemplateId) {
        ItemFormTemplate update = new ItemFormTemplate();
        update.setStatus((byte) 0);

        this.update(update, new LambdaQueryWrapper<ItemFormTemplate>()
                .eq(ItemFormTemplate::getItemId, itemId)
                .eq(ItemFormTemplate::getStatus, 1)
                .ne(ItemFormTemplate::getTemplateId, excludeTemplateId));
    }

    /**
     * 实体转VO
     */
    private ItemFormTemplateVO convertToVO(ItemFormTemplate template) {
        ItemFormTemplateVO vo = new ItemFormTemplateVO();
        BeanUtils.copyProperties(template, vo);

        // 查询事项名称
        if (template.getItemId() != null) {
            ItemInfo itemInfo = itemInfoMapper.selectById(template.getItemId());
            if (itemInfo != null) {
                vo.setItemName(itemInfo.getItemName());
            }
        }

        // 状态文本
        vo.setStatusText(template.getStatus() != null && template.getStatus() == 1 ? "已启用" : "草稿");

        return vo;
    }
}
