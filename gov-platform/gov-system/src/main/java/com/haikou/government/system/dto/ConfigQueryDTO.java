package com.haikou.government.system.dto;

import com.haikou.government.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统参数查询参数
 *
 * @author xhl
 * @since 2026-08-04
 */
@Schema(description = "系统参数查询参数")
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigQueryDTO extends PageQuery {

    @Schema(description = "参数名称")
    private String configName;

    @Schema(description = "参数键名")
    private String configKey;
}
