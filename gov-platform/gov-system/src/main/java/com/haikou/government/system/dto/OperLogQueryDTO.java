package com.haikou.government.system.dto;

import com.haikou.government.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志查询参数
 *
 * @author xhl
 * @since 2026-08-04
 */
@Schema(description = "操作日志查询参数")
@Data
@EqualsAndHashCode(callSuper = true)
public class OperLogQueryDTO extends PageQuery {

    @Schema(description = "模块标题")
    private String title;

    @Schema(description = "操作人员")
    private String operName;

    @Schema(description = "操作状态（0正常 1异常）")
    private Byte status;

    @Schema(description = "开始时间")
    private LocalDateTime beginTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
