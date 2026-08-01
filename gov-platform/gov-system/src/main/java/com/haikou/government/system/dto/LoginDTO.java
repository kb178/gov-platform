package com.haikou.government.system.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 用户登录 DTO（Data Transfer Object）
 *
 * 作用：封装登录请求的参数，用于 Controller 接收前端传递的数据
 * 为什么用 DTO：不直接使用实体类，可以隐藏敏感字段、添加参数校验
 *
 * @author xhl
 * @since 2026-08-01
 */
@Data
public class LoginDTO {

    /**
     * 手机号（登录账号）
     *
     * 校验规则：
     * - @NotBlank: 不能为空
     * - @Pattern: 必须符合手机号格式（1开头，第二位3-9，后面9位数字）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 登录密码
     *
     * 校验规则：
     * - @NotBlank: 不能为空
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
