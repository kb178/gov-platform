package com.haikou.government.item.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.common.core.domain.R;
import com.haikou.government.item.dto.ItemFavoriteDTO;
import com.haikou.government.item.service.ItemFavoriteService;
import com.haikou.government.item.vo.ItemFavoriteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 事项收藏管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-10
 */
@Tag(name = "事项收藏管理", description = "老百姓收藏事项接口")
@RestController
@RequestMapping("/itemFavorite")
public class ItemFavoriteController {

    @Autowired
    private ItemFavoriteService itemFavoriteService;

    /**
     * 收藏事项
     *
     * @param dto 收藏信息
     * @return 收藏ID
     */
    @Operation(summary = "收藏事项", description = "用户收藏事项")
    @PostMapping
    public R<Long> add(@Valid @RequestBody ItemFavoriteDTO dto) {
        Long id = itemFavoriteService.addFavorite(dto.getUserId(), dto.getItemId());
        return R.ok(id);
    }

    /**
     * 取消收藏
     *
     * @param dto 收藏信息
     * @return 是否成功
     */
    @Operation(summary = "取消收藏", description = "用户取消收藏事项")
    @DeleteMapping
    public R<Boolean> remove(@Valid @RequestBody ItemFavoriteDTO dto) {
        itemFavoriteService.removeFavorite(dto.getUserId(), dto.getItemId());
        return R.ok(true);
    }

    /**
     * 查询是否已收藏
     *
     * @param userId 用户ID
     * @param itemId 事项ID
     * @return 是否已收藏
     */
    @Operation(summary = "查询是否已收藏", description = "查询用户是否已收藏该事项")
    @GetMapping("/check")
    public R<Boolean> check(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "事项ID") @RequestParam Long itemId) {
        boolean isFavorite = itemFavoriteService.isFavorite(userId, itemId);
        return R.ok(isFavorite);
    }

    /**
     * 查询用户的收藏列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param userId   用户ID
     * @return 收藏列表
     */
    @Operation(summary = "查询用户的收藏列表", description = "分页查询用户的收藏事项")
    @GetMapping("/list")
    public R<IPage<ItemFavoriteVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam Long userId) {

        Page<ItemFavoriteVO> page = new Page<>(pageNum, pageSize);
        IPage<ItemFavoriteVO> result = itemFavoriteService.getFavoritePageList(page, userId);
        return R.ok(result);
    }
}
