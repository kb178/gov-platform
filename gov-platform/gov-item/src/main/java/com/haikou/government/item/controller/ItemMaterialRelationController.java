package com.haikou.government.item.controller;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.item.dto.ItemMaterialRelationDTO;
import com.haikou.government.item.service.ItemMaterialRelationService;
import com.haikou.government.item.vo.ItemMaterialRelationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 事项材料关联管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-10
 */
@Tag(name = "事项材料关联管理", description = "事项与材料关联关系接口")
@RestController
@RequestMapping("/itemMaterialRelation")
public class ItemMaterialRelationController {

    @Autowired
    private ItemMaterialRelationService itemMaterialRelationService;

    /**
     * 查询事项的材料列表
     *
     * @param itemId 事项ID
     * @return 材料关联列表
     */
    @Operation(summary = "查询事项的材料列表", description = "根据事项ID查询该事项所需的所有材料")
    @GetMapping("/item/{itemId}")
    public R<List<ItemMaterialRelationVO>> listByItemId(@PathVariable("itemId") Long itemId) {
        List<ItemMaterialRelationVO> list = itemMaterialRelationService.getMaterialsByItemId(itemId);
        return R.ok(list);
    }

    /**
     * 批量设置事项的材料
     *
     * @param dto 材料关联信息
     * @return 是否成功
     */
    @Operation(summary = "批量设置事项的材料", description = "为事项配置所需材料清单（先删后增）")
    @PostMapping("/set")
    public R<Boolean> setItemMaterials(@Valid @RequestBody ItemMaterialRelationDTO dto) {
        itemMaterialRelationService.setItemMaterials(dto);
        return R.ok(true);
    }

    /**
     * 删除事项的某个材料关联
     *
     * @param id 关联ID
     * @return 是否成功
     */
    @Operation(summary = "删除材料关联", description = "删除事项与材料的关联关系")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        itemMaterialRelationService.deleteRelation(id);
        return R.ok(true);
    }

    /**
     * 删除事项的所有材料关联
     *
     * @param itemId 事项ID
     * @return 是否成功
     */
    @Operation(summary = "删除事项的所有材料关联", description = "清空事项的材料清单")
    @DeleteMapping("/item/{itemId}")
    public R<Boolean> deleteAllByItemId(@PathVariable("itemId") Long itemId) {
        itemMaterialRelationService.deleteAllByItemId(itemId);
        return R.ok(true);
    }
}
