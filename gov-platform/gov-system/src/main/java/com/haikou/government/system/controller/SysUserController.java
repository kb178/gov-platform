package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.system.dto.LoginDTO;
import com.haikou.government.system.dto.RegisterDTO;
import com.haikou.government.system.dto.SmsLoginDTO;
import com.haikou.government.system.vo.LoginVO;
import com.haikou.government.system.service.SmsService;
import com.haikou.government.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户表 前端控制器
 *
 * @author xhl
 * @since 2026-08-01
 */
@Tag(name = "用户管理", description = "用户注册、登录、验证码等接口")
@RestController
@RequestMapping("/sysUser")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SmsService smsService;

    /**
     * 发送短信验证码
     */
    @Operation(summary = "发送短信验证码", description = "向指定手机号发送6位验证码，有效期5分钟")
    @PostMapping("/sendCode")
    public R<Boolean> sendCode(
            @Parameter(description = "手机号", required = true, example = "13800138000")
            @RequestParam("phone") String phone) {
        boolean result = smsService.sendCode(phone);
        return R.ok(result);
    }

    /**
     * 手机号注册
     */
    @Operation(summary = "手机号注册", description = "使用手机号、密码、验证码注册新用户")
    @PostMapping("/register")
    public R<Boolean> register(@Valid @RequestBody RegisterDTO registerDTO) {
        boolean result = sysUserService.register(registerDTO);
        return R.ok(result);
    }

    /**
     * 密码登录
     *
     * @param loginDTO 登录参数
     * @return LoginVO 登录成功信息
     */
    @Operation(summary = "密码登录", description = "使用手机号和密码登录，连续失败5次将锁定15分钟")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        // IP 获取已通过拦截器存入 ThreadLocal，Service 层直接获取即可
        LoginVO loginVO = sysUserService.login(loginDTO);
        return R.ok(loginVO);
    }

    /**
     * 验证码登录
     *
     * @param smsLoginDTO 登录参数（手机号 + 验证码）
     * @return LoginVO 登录成功信息
     */
    @Operation(summary = "验证码登录", description = "使用手机号和短信验证码登录，未注册用户自动注册")
    @PostMapping("/smsLogin")
    public R<LoginVO> smsLogin(@Valid @RequestBody SmsLoginDTO smsLoginDTO) {
        LoginVO loginVO = sysUserService.smsLogin(smsLoginDTO);
        return R.ok(loginVO);
    }
}
