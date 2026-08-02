package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.common.core.utils.HttpRequestHolder;
import com.haikou.government.system.dto.LoginDTO;
import com.haikou.government.system.dto.RealNameDTO;
import com.haikou.government.system.dto.RegisterDTO;
import com.haikou.government.system.dto.SmsLoginDTO;
import com.haikou.government.system.vo.LoginVO;
import com.haikou.government.system.vo.RealNameVO;
import com.haikou.government.system.service.SmsService;
import com.haikou.government.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

    /**
     * 实名认证
     *
     * @param realNameDTO 实名认证参数（姓名 + 身份证号）
     * @return 是否认证成功
     */
    @Operation(summary = "实名认证", description = "提交身份证号和姓名进行实名认证")
    @PostMapping("/realNameAuth")
    public R<Boolean> realNameAuth(@Valid @RequestBody RealNameDTO realNameDTO) {
        // 从 Token 中获取用户ID（由 Gateway 传递）
        Long userId = getCurrentUserId();
        boolean result = sysUserService.realNameAuth(userId, realNameDTO);
        return R.ok(result);
    }

    /**
     * 查询实名认证状态
     *
     * @return 实名认证信息
     */
    @Operation(summary = "查询实名认证状态", description = "获取当前用户的实名认证信息")
    @GetMapping("/realNameStatus")
    public R<RealNameVO> getRealNameStatus() {
        // 从 Token 中获取用户ID
        Long userId = getCurrentUserId();
        RealNameVO realNameVO = sysUserService.getRealNameStatus(userId);
        return R.ok(realNameVO);
    }

    /**
     * 获取当前登录用户ID
     *
     * 从 Gateway 传递的请求头中获取用户ID
     * Gateway 的 AuthFilter 会解析 Token，将用户ID放入请求头 X-User-Id
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        // 从请求头中获取用户ID（Gateway 传递）
        HttpServletRequest request = HttpRequestHolder.get();
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new BusinessException("用户未登录");
        }
        return Long.parseLong(userIdStr);
    }
}
