package com.haikou.government.item.controller;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.item.dto.ItemCategoryDTO;
import com.haikou.government.item.service.ItemCategoryService;
import com.haikou.government.item.vo.ItemCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 事项分类管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-10
 */
@Tag(name = "事项分类管理", description = "事项分类增删改查接口")
@RestController
@RequestMapping("/itemCategory")
public class ItemCategoryController {

    @Autowired
    private ItemCategoryService itemCategoryService;

    /**
     * 查询分类树形列表
     *
     * @return 树形分类列表
     */
    @Operation(summary = "查询分类树形列表", description = "获取所有分类的树形结构")
    @GetMapping("/tree")
    public R<List<ItemCategoryVO>> tree() {
        List<ItemCategoryVO> tree = itemCategoryService.getCategoryTree();
        return R.ok(tree);
    }

    /**
     * 查询分类列表
     *
     * @param parentId 父分类ID（可选）
     * @return 分类列表
     */
    @Operation(summary = "查询分类列表", description = "根据父分类ID查询子分类列表")
    @GetMapping("/list")
    public R<List<ItemCategoryVO>> list(@RequestParam(value = "parentId", required = false) Long parentId) {
        List<ItemCategoryVO> list = itemCategoryService.getCategoryList(parentId);
        return R.ok(list);
    }

    /**
     * 查询分类详情
     *
     * @param categoryId 分类ID
     * @return 分类信息
     */
    @Operation(summary = "查询分类详情", description = "根据分类ID查询详情")
    @GetMapping("/{categoryId}")
    public R<ItemCategoryVO> getById(@PathVariable("categoryId") Long categoryId) {
        ItemCategoryVO vo = itemCategoryService.getCategoryById(categoryId);
        return R.ok(vo);
    }

    /**
     * 新增分类
     *
     * @param dto 分类参数
     * @return 是否成功
     */
    @Operation(summary = "新增分类", description = "添加新事项分类")
    @PostMapping
    public R<Boolean> add(@Valid @RequestBody ItemCategoryDTO dto) {
        boolean result = itemCategoryService.addCategory(dto);
        return R.ok(result);
    }

    /**
     * 修改分类
     *
     * @param dto 分类参数
     * @return 是否成功
     */
    @Operation(summary = "修改分类", description = "修改事项分类信息")
    @PutMapping
    public R<Boolean> update(@Valid @RequestBody ItemCategoryDTO dto) {
        boolean result = itemCategoryService.updateCategory(dto);
        return R.ok(result);
    }

    /**
     * 删除分类
     *
     * @param categoryId 分类ID
     * @return 是否成功
     */
    @Operation(summary = "删除分类", description = "删除事项分类")
    @DeleteMapping("/{categoryId}")
    public R<Boolean> delete(@PathVariable("categoryId") Long categoryId) {
        boolean result = itemCategoryService.deleteCategory(categoryId);
        return R.ok(result);
    }
}
