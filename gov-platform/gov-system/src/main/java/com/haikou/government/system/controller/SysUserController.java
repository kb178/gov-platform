package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.system.dto.LoginDTO;
import com.haikou.government.system.dto.RegisterDTO;
import com.haikou.government.system.dto.SmsLoginDTO;
import com.haikou.government.system.vo.LoginVO;
import com.haikou.government.system.service.SmsService;
import com.haikou.government.system.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户表 前端控制器
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
     * 发送短信验证码
     */
    @PostMapping("/sendCode")
    public R<Boolean> sendCode(@RequestParam("phone") String phone) {
        boolean result = smsService.sendCode(phone);
        return R.ok(result);
    }

    /**
     * 手机号注册
     */
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
    @PostMapping("/smsLogin")
    public R<LoginVO> smsLogin(@Valid @RequestBody SmsLoginDTO smsLoginDTO) {
        LoginVO loginVO = sysUserService.smsLogin(smsLoginDTO);
        return R.ok(loginVO);
    }
}
