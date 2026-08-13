package com.haikou.government.item.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.common.core.domain.R;
import com.haikou.government.item.dto.ItemMaterialTemplateDTO;
import com.haikou.government.item.service.ItemMaterialTemplateService;
import com.haikou.government.item.vo.ItemMaterialTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 材料模板管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-10
 */
@Tag(name = "材料模板管理", description = "材料模板增删改查接口")
@RestController
@RequestMapping("/itemMaterialTemplate")
public class ItemMaterialTemplateController {

    @Autowired
    private ItemMaterialTemplateService itemMaterialTemplateService;

    /**
     * 分页查询材料模板列表
     *
     * @param pageNum      页码
     * @param pageSize     每页数量
     * @param materialName 材料名称（可选，模糊查询）
     * @param status       状态（可选）
     * @return 材料模板分页列表
     */
    @Operation(summary = "分页查询材料模板列表", description = "支持按名称、状态筛选")
    @GetMapping("/list")
    public R<IPage<ItemMaterialTemplateVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "材料名称") @RequestParam(required = false) String materialName,
            @Parameter(description = "状态（0正常 1停用）") @RequestParam(required = false) Byte status) {

        Page<ItemMaterialTemplateVO> page = new Page<>(pageNum, pageSize);
        IPage<ItemMaterialTemplateVO> result = itemMaterialTemplateService.getMaterialPageList(page, materialName, status);
        return R.ok(result);
    }

    /**
     * 查询材料模板详情
     *
     * @param materialId 材料ID
     * @return 材料模板详情
     */
    @Operation(summary = "查询材料模板详情", description = "根据材料ID查询详情")
    @GetMapping("/{materialId}")
    public R<ItemMaterialTemplateVO> getById(@PathVariable("materialId") Long materialId) {
        ItemMaterialTemplateVO vo = itemMaterialTemplateService.getMaterialDetail(materialId);
        return R.ok(vo);
    }

    /**
     * 查询所有正常状态的材料模板
     *
     * @return 材料模板列表（用于下拉选择）
     */
    @Operation(summary = "查询所有可用材料模板", description = "获取所有正常状态的材料模板（用于下拉选择）")
    @GetMapping("/active")
    public R<List<ItemMaterialTemplateVO>> getAllActive() {
        List<ItemMaterialTemplateVO> list = itemMaterialTemplateService.getAllActiveMaterials();
        return R.ok(list);
    }

    /**
     * 新增材料模板
     *
     * @param dto 材料模板参数
     * @return 材料ID
     */
    @Operation(summary = "新增材料模板", description = "添加新材料模板")
    @PostMapping
    public R<Long> add(@Valid @RequestBody ItemMaterialTemplateDTO dto) {
        Long materialId = itemMaterialTemplateService.addMaterial(dto);
        return R.ok(materialId);
    }

    /**
     * 修改材料模板
     *
     * @param dto 材料模板参数
     * @return 是否成功
     */
    @Operation(summary = "修改材料模板", description = "修改材料模板信息")
    @PutMapping
    public R<Boolean> update(@Valid @RequestBody ItemMaterialTemplateDTO dto) {
        itemMaterialTemplateService.updateMaterial(dto);
        return R.ok(true);
    }

    /**
     * 删除材料模板
     *
     * @param materialId 材料ID
     * @return 是否成功
     */
    @Operation(summary = "删除材料模板", description = "删除材料模板（被事项引用的不能删除）")
    @DeleteMapping("/{materialId}")
    public R<Boolean> delete(@PathVariable("materialId") Long materialId) {
        itemMaterialTemplateService.deleteMaterial(materialId);
        return R.ok(true);
    }
}
