package com.haikou.government.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 管理员登录 DTO
 *
 * 作用：封装管理端登录请求的参数（用户名 + 密码）
 *
 * @author xhl
 * @since 2026-09-02
 */
@Schema(description = "管理员登录请求参数")
@Data
public class AdminLoginDTO {

    /**
     * 用户名
     *
     * 校验规则：
     * - @NotBlank: 不能为空
     */
    @Schema(description = "用户名", required = true, example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 登录密码
     *
     * 校验规则：
     * - @NotBlank: 不能为空
     */
    @Schema(description = "登录密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;
}
