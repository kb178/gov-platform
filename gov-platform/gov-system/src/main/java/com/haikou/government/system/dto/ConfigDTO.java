package com.haikou.government.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 系统参数请求参数
 *
 * @author xhl
 * @since 2026-08-04
 */
@Schema(description = "系统参数请求参数")
@Data
public class ConfigDTO {

    @Schema(description = "参数ID（修改时必填）")
    private Long configId;

    @Schema(description = "参数名称")
    @NotBlank(message = "参数名称不能为空")
    private String configName;

    @Schema(description = "参数键名")
    @NotBlank(message = "参数键名不能为空")
    private String configKey;

    @Schema(description = "参数键值")
    @NotBlank(message = "参数键值不能为空")
    private String configValue;

    @Schema(description = "备注")
    private String remark;
}
