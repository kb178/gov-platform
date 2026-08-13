package com.haikou.government.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 事项收藏 DTO（请求）
 *
 * @author xhl
 * @since 2026-08-13
 */
@Data
@Schema(description = "事项收藏DTO")
public class ItemFavoriteDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @NotNull(message = "事项ID不能为空")
    @Schema(description = "事项ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long itemId;
}
