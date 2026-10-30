package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.PageResult;
import com.haikou.government.common.core.domain.R;
import com.haikou.government.common.security.utils.SecurityUtils;
import com.haikou.government.system.annotation.Log;
import com.haikou.government.system.dto.AdminLoginDTO;
import com.haikou.government.system.dto.ChangePasswordDTO;
import com.haikou.government.system.dto.LoginDTO;
import com.haikou.government.system.dto.RealNameDTO;
import com.haikou.government.system.dto.RegisterDTO;
import com.haikou.government.system.dto.ResetPasswordDTO;
import com.haikou.government.system.dto.SmsLoginDTO;
import com.haikou.government.system.dto.UpdateUserDTO;
import com.haikou.government.system.dto.UserAddDTO;
import com.haikou.government.system.dto.UserQueryDTO;
import com.haikou.government.system.dto.UserUpdateDTO;
import com.haikou.government.system.enums.BusinessType;
import com.haikou.government.system.vo.LoginVO;
import com.haikou.government.system.vo.RealNameVO;
import com.haikou.government.system.vo.UserInfoVO;
import com.haikou.government.system.vo.UserVO;
import com.haikou.government.system.service.SmsService;
import com.haikou.government.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
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
    @Log(title = "用户管理", businessType = BusinessType.QUERY)
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
     * 管理员登录
     *
     * @param adminLoginDTO 登录参数（用户名 + 密码）
     * @return LoginVO 登录成功信息
     */
    @Log(title = "管理员登录", businessType = BusinessType.QUERY)
    @Operation(summary = "管理员登录", description = "管理端使用用户名和密码登录，仅限工作人员账号")
    @PostMapping("/adminLogin")
    public R<LoginVO> adminLogin(@Valid @RequestBody AdminLoginDTO adminLoginDTO) {
        LoginVO loginVO = sysUserService.adminLogin(adminLoginDTO);
        return R.ok(loginVO);
    }

    /**
     * 实名认证
     *
     * @param realNameDTO 实名认证参数（姓名 + 身份证号）
     * @return 是否认证成功
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "实名认证", description = "提交身份证号和姓名进行实名认证")
    @PostMapping("/realNameAuth")
    public R<Boolean> realNameAuth(@Valid @RequestBody RealNameDTO realNameDTO) {
        // 使用 SecurityUtils 获取当前登录用户ID
        Long userId = SecurityUtils.getCurrentUserId();
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
        // 使用 SecurityUtils 获取当前登录用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        RealNameVO realNameVO = sysUserService.getRealNameStatus(userId);
        return R.ok(realNameVO);
    }

    /**
     * 修改密码
     *
     * @param changePasswordDTO 修改密码参数（旧密码、新密码、确认密码）
     * @return 是否修改成功
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改密码", description = "已登录用户修改密码")
    @PostMapping("/changePassword")
    public R<Boolean> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        // 使用 SecurityUtils 获取当前登录用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        boolean result = sysUserService.changePassword(userId, changePasswordDTO);
        return R.ok(result);
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public R<UserInfoVO> getUserInfo() {
        // 使用 SecurityUtils 获取当前登录用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        UserInfoVO userInfoVO = sysUserService.getUserInfo(userId);
        return R.ok(userInfoVO);
    }

    /**
     * 修改个人信息
     *
     * @param updateUserDTO 修改参数（昵称、头像、性别、邮箱）
     * @return 是否修改成功
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改个人信息", description = "修改当前登录用户的昵称、头像、性别、邮箱")
    @PutMapping("/info")
    public R<Boolean> updateUserInfo(@Valid @RequestBody UpdateUserDTO updateUserDTO) {
        // 使用 SecurityUtils 获取当前登录用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        boolean result = sysUserService.updateUserInfo(userId, updateUserDTO);
        return R.ok(result);
    }

    /**
     * 找回密码（重置密码）
     *
     * @param resetPasswordDTO 重置密码参数（手机号、验证码、新密码、确认密码）
     * @return 是否重置成功
     */
    @Operation(summary = "找回密码", description = "通过手机号验证码重置密码")
    @PostMapping("/resetPassword")
    public R<Boolean> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        boolean result = sysUserService.resetPassword(resetPasswordDTO);
        return R.ok(result);
    }

    // ==================== 管理端接口 ====================

    /**
     * 用户分页列表（管理端）
     */
    @Operation(summary = "用户分页列表", description = "管理员查看用户列表，支持按用户名、手机号、用户类型、状态筛选")
    @GetMapping("/admin/page")
    public R<PageResult<UserVO>> getUserPage(UserQueryDTO queryDTO) {
        PageResult<UserVO> pageResult = sysUserService.getUserPage(queryDTO);
        return R.ok(pageResult);
    }

    /**
     * 用户详情（管理端）
     */
    @Operation(summary = "用户详情", description = "管理员查看用户详细信息")
    @GetMapping("/admin/{userId}")
    public R<UserVO> getUserDetail(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId) {
        UserVO userVO = sysUserService.getUserDetail(userId);
        return R.ok(userVO);
    }

    /**
     * 新增用户（管理端）
     */
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增用户", description = "管理员创建新用户")
    @PostMapping("/admin")
    public R<Boolean> addUser(@Valid @RequestBody UserAddDTO userAddDTO) {
        boolean result = sysUserService.addUser(userAddDTO);
        return R.ok(result);
    }

    /**
     * 修改用户（管理端）
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改用户", description = "管理员修改用户信息")
    @PutMapping("/admin")
    public R<Boolean> updateUser(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        boolean result = sysUserService.updateUser(userUpdateDTO);
        return R.ok(result);
    }

    /**
     * 删除用户（管理端）
     */
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除用户", description = "管理员删除用户（逻辑删除）")
    @DeleteMapping("/admin/{userId}")
    public R<Boolean> deleteUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId) {
        boolean result = sysUserService.deleteUser(userId);
        return R.ok(result);
    }

    /**
     * 重置用户密码（管理端）
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "重置密码", description = "管理员重置用户密码为默认密码（123456）")
    @PutMapping("/admin/resetPassword/{userId}")
    public R<Boolean> resetUserPassword(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId) {
        boolean result = sysUserService.resetUserPassword(userId);
        return R.ok(result);
    }

    /**
     * 分配用户角色（管理端）
     */
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @Operation(summary = "分配角色", description = "管理员为用户分配角色")
    @PutMapping("/admin/assignRoles/{userId}")
    public R<Boolean> assignRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId,
            @Parameter(description = "角色ID列表", required = true)
            @RequestBody List<Long> roleIds) {
        boolean result = sysUserService.assignRoles(userId, roleIds);
        return R.ok(result);
    }
}
