package com.haikou.government.item.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.common.core.domain.R;
import com.haikou.government.item.dto.ItemInfoDTO;
import com.haikou.government.item.service.ItemInfoService;
import com.haikou.government.item.vo.ItemInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 事项信息管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-10
 */
@Tag(name = "事项信息管理", description = "事项信息增删改查接口")
@RestController
@RequestMapping("/itemInfo")
public class ItemInfoController {

    @Autowired
    private ItemInfoService itemInfoService;

    /**
     * 分页查询事项列表
     *
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @param categoryId 分类ID（可选）
     * @param itemName   事项名称（可选，模糊查询）
     * @param status     状态（可选）
     * @return 事项分页列表
     */
    @Operation(summary = "分页查询事项列表", description = "支持按分类、名称、状态筛选")
    @GetMapping("/list")
    public R<IPage<ItemInfoVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "事项名称") @RequestParam(required = false) String itemName,
            @Parameter(description = "状态（0草稿 1已发布 2已下线）") @RequestParam(required = false) Byte status) {

        Page<ItemInfoVO> page = new Page<>(pageNum, pageSize);
        IPage<ItemInfoVO> result = itemInfoService.getItemPageList(page, categoryId, itemName, status);
        return R.ok(result);
    }

    /**
     * 查询事项详情
     *
     * @param itemId 事项ID
     * @return 事项详情
     */
    @Operation(summary = "查询事项详情", description = "根据事项ID查询详情")
    @GetMapping("/{itemId}")
    public R<ItemInfoVO> getById(@PathVariable("itemId") Long itemId) {
        ItemInfoVO vo = itemInfoService.getItemDetail(itemId);
        return R.ok(vo);
    }

    /**
     * 新增事项
     *
     * @param dto 事项参数
     * @return 事项ID
     */
    @Operation(summary = "新增事项", description = "添加新事项信息")
    @PostMapping
    public R<Long> add(@Valid @RequestBody ItemInfoDTO dto) {
        Long itemId = itemInfoService.addItem(dto);
        return R.ok(itemId);
    }

    /**
     * 修改事项
     *
     * @param dto 事项参数
     * @return 是否成功
     */
    @Operation(summary = "修改事项", description = "修改事项信息")
    @PutMapping
    public R<Boolean> update(@Valid @RequestBody ItemInfoDTO dto) {
        itemInfoService.updateItem(dto);
        return R.ok(true);
    }

    /**
     * 删除事项
     *
     * @param itemId 事项ID
     * @return 是否成功
     */
    @Operation(summary = "删除事项", description = "删除事项（草稿/已下线状态才能删除）")
    @DeleteMapping("/{itemId}")
    public R<Boolean> delete(@PathVariable("itemId") Long itemId) {
        itemInfoService.deleteItem(itemId);
        return R.ok(true);
    }

    /**
     * 发布事项
     *
     * @param itemId 事项ID
     * @return 是否成功
     */
    @Operation(summary = "发布事项", description = "发布事项（草稿状态才能发布）")
    @PutMapping("/publish/{itemId}")
    public R<Boolean> publish(@PathVariable("itemId") Long itemId) {
        itemInfoService.publishItem(itemId);
        return R.ok(true);
    }

    /**
     * 下线事项
     *
     * @param itemId 事项ID
     * @return 是否成功
     */
    @Operation(summary = "下线事项", description = "下线事项（已发布状态才能下线）")
    @PutMapping("/offline/{itemId}")
    public R<Boolean> offline(@PathVariable("itemId") Long itemId) {
        itemInfoService.offlineItem(itemId);
        return R.ok(true);
    }
}
