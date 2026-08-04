package com.haikou.government.system.dto;

import com.haikou.government.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志查询参数
 *
 * @author xhl
 * @since 2026-08-04
 */
@Schema(description = "登录日志查询参数")
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogQueryDTO extends PageQuery {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录状态（0成功 1失败）")
    private Byte status;

    @Schema(description = "开始时间")
    private LocalDateTime beginTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
