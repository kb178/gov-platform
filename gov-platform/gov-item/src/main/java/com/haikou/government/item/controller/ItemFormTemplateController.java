package com.haikou.government.item.controller;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.item.dto.ItemFormTemplateDTO;
import com.haikou.government.item.service.ItemFormTemplateService;
import com.haikou.government.item.vo.ItemFormTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表单模板管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-10
 */
@Tag(name = "表单模板管理", description = "事项表单模板增删改查接口")
@RestController
@RequestMapping("/itemFormTemplate")
public class ItemFormTemplateController {

    @Autowired
    private ItemFormTemplateService itemFormTemplateService;

    /**
     * 查询事项的表单模板列表
     *
     * @param itemId 事项ID
     * @return 模板列表
     */
    @Operation(summary = "查询事项的表单模板列表", description = "根据事项ID查询该事项下的所有表单模板")
    @GetMapping("/item/{itemId}")
    public R<List<ItemFormTemplateVO>> listByItemId(@PathVariable("itemId") Long itemId) {
        List<ItemFormTemplateVO> list = itemFormTemplateService.getTemplatesByItemId(itemId);
        return R.ok(list);
    }

    /**
     * 查询模板详情
     *
     * @param templateId 模板ID
     * @return 模板详情
     */
    @Operation(summary = "查询模板详情", description = "根据模板ID查询详情")
    @GetMapping("/{templateId}")
    public R<ItemFormTemplateVO> getById(@PathVariable("templateId") Long templateId) {
        ItemFormTemplateVO vo = itemFormTemplateService.getTemplateDetail(templateId);
        return R.ok(vo);
    }

    /**
     * 获取事项当前启用的模板
     *
     * @param itemId 事项ID
     * @return 启用的模板
     */
    @Operation(summary = "获取事项当前启用的模板", description = "获取事项当前启用的表单模板（用于前端渲染申请表单）")
    @GetMapping("/active/{itemId}")
    public R<ItemFormTemplateVO> getActiveTemplate(@PathVariable("itemId") Long itemId) {
        ItemFormTemplateVO vo = itemFormTemplateService.getActiveTemplate(itemId);
        return R.ok(vo);
    }

    /**
     * 新增模板
     *
     * @param dto 模板参数
     * @return 模板ID
     */
    @Operation(summary = "新增模板", description = "添加新表单模板")
    @PostMapping
    public R<Long> add(@Valid @RequestBody ItemFormTemplateDTO dto) {
        Long templateId = itemFormTemplateService.addTemplate(dto);
        return R.ok(templateId);
    }

    /**
     * 修改模板
     *
     * @param dto 模板参数
     * @return 是否成功
     */
    @Operation(summary = "修改模板", description = "修改表单模板（已启用的模板不能修改）")
    @PutMapping
    public R<Boolean> update(@Valid @RequestBody ItemFormTemplateDTO dto) {
        itemFormTemplateService.updateTemplate(dto);
        return R.ok(true);
    }

    /**
     * 删除模板
     *
     * @param templateId 模板ID
     * @return 是否成功
     */
    @Operation(summary = "删除模板", description = "删除表单模板（已启用的模板不能删除）")
    @DeleteMapping("/{templateId}")
    public R<Boolean> delete(@PathVariable("templateId") Long templateId) {
        itemFormTemplateService.deleteTemplate(templateId);
        return R.ok(true);
    }

    /**
     * 启用模板
     *
     * @param templateId 模板ID
     * @return 是否成功
     */
    @Operation(summary = "启用模板", description = "启用模板（同一事项下只允许一个启用的模板）")
    @PutMapping("/enable/{templateId}")
    public R<Boolean> enable(@PathVariable("templateId") Long templateId) {
        itemFormTemplateService.enableTemplate(templateId);
        return R.ok(true);
    }

    /**
     * 禁用模板
     *
     * @param templateId 模板ID
     * @return 是否成功
     */
    @Operation(summary = "禁用模板", description = "禁用模板")
    @PutMapping("/disable/{templateId}")
    public R<Boolean> disable(@PathVariable("templateId") Long templateId) {
        itemFormTemplateService.disableTemplate(templateId);
        return R.ok(true);
    }
}
