package com.haikou.government.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事项收藏 VO（响应）
 *
 * @author xhl
 * @since 2026-08-13
 */
@Data
@Schema(description = "事项收藏VO")
public class ItemFavoriteVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "事项ID")
    private Long itemId;

    @Schema(description = "事项名称")
    private String itemName;

    @Schema(description = "事项编码")
    private String itemCode;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "简短描述")
    private String summary;

    @Schema(description = "收藏时间")
    private LocalDateTime createTime;
}
