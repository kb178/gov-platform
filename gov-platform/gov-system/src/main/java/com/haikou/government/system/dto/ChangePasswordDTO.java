package com.haikou.government.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求参数
 *
 * @author 海口政务开发团队
 * @since 2026-08-01
 */
@Schema(description = "修改密码请求参数")
@Data
public class ChangePasswordDTO {

    @Schema(description = "旧密码", required = true, example = "123456")
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码", required = true, example = "654321")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 位之间")
    private String newPassword;

    @Schema(description = "确认新密码", required = true, example = "654321")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
