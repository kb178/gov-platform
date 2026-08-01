package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.system.domain.dto.RegisterDTO;
import com.haikou.government.system.service.SmsService;
import com.haikou.government.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author xhl
 * @since 2026-08-01
 */
@RestController
@RequestMapping("/sysUser")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SmsService smsService;

    /**
     * 发送验证码
     */
    @PostMapping("/sendCode")
    public R<Boolean> sendCode(@RequestParam("phone") String phone) {
        boolean result = smsService.sendCode(phone);
        return R.ok(result, "验证码发送成功");
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public R<Boolean> register(@Valid @RequestBody RegisterDTO registerDTO) {
        boolean result = sysUserService.register(registerDTO);
        return R.ok(result, "注册成功");
    }
}
