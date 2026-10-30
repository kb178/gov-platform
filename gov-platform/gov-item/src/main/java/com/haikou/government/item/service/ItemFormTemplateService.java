package com.haikou.government.item.service;

import com.haikou.government.item.domain.ItemFormTemplate;
import com.baomidou.mybatisplus.extension.service.IService;
import com.haikou.government.item.dto.ItemFormTemplateDTO;
import com.haikou.government.item.vo.ItemFormTemplateVO;

import java.util.List;

/**
 * 事项表单模板表 服务接口
 *
 * @author xhl
 * @since 2026-08-10
 */
public interface ItemFormTemplateService extends IService<ItemFormTemplate> {

    /**
     * 查询事项的表单模板列表
     *
     * @param itemId 事项ID
     * @return 模板列表
     */
    List<ItemFormTemplateVO> getTemplatesByItemId(Long itemId);

    /**
     * 查询模板详情
     *
     * @param templateId 模板ID
     * @return 模板详情
     */
    ItemFormTemplateVO getTemplateDetail(Long templateId);

    /**
     * 新增模板
     *
     * @param dto 模板信息
     * @return 模板ID
     */
    Long addTemplate(ItemFormTemplateDTO dto);

    /**
     * 修改模板
     *
     * @param dto 模板信息
     */
    void updateTemplate(ItemFormTemplateDTO dto);

    /**
     * 删除模板
     *
     * @param templateId 模板ID
     */
    void deleteTemplate(Long templateId);

    /**
     * 启用模板
     *
     * @param templateId 模板ID
     */
    void enableTemplate(Long templateId);

    /**
     * 禁用模板
     *
     * @param templateId 模板ID
     */
    void disableTemplate(Long templateId);

    /**
     * 获取事项当前启用的模板
     *
     * @param itemId 事项ID
     * @return 启用的模板（可能为null）
     */
    ItemFormTemplateVO getActiveTemplate(Long itemId);
}
