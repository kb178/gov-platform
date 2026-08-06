package com.haikou.government.system.service;

import com.haikou.government.common.core.domain.PageResult;
import com.haikou.government.system.domain.SysUser;
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
import com.haikou.government.system.vo.LoginVO;
import com.haikou.government.system.vo.RealNameVO;
import com.haikou.government.system.vo.UserInfoVO;
import com.haikou.government.system.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author xhl
 * @since 2026-08-01
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     * @return 是否成功
     */
    boolean register(RegisterDTO registerDTO);

    /**
     * 密码登录
     *
     * @param loginDTO 登录参数（手机号 + 密码）
     * @return LoginVO 登录成功信息（包含 Token）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 验证码登录
     *
     * @param smsLoginDTO 登录参数（手机号 + 验证码）
     * @return LoginVO 登录成功信息（包含 Token）
     */
    LoginVO smsLogin(SmsLoginDTO smsLoginDTO);

    /**
     * 实名认证
     *
     * @param userId 用户ID
     * @param realNameDTO 实名认证参数（姓名 + 身份证号）
     * @return 是否认证成功
     */
    boolean realNameAuth(Long userId, RealNameDTO realNameDTO);

    /**
     * 查询实名认证状态
     *
     * @param userId 用户ID
     * @return 实名认证信息
     */
    RealNameVO getRealNameStatus(Long userId);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param changePasswordDTO 修改密码参数（旧密码、新密码、确认密码）
     * @return 是否修改成功
     */
    boolean changePassword(Long userId, ChangePasswordDTO changePasswordDTO);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 修改个人信息
     *
     * @param userId 用户ID
     * @param updateUserDTO 修改参数（昵称、头像、性别、邮箱）
     * @return 是否修改成功
     */
    boolean updateUserInfo(Long userId, UpdateUserDTO updateUserDTO);

    /**
     * 找回密码（重置密码）
     *
     * @param resetPasswordDTO 重置密码参数（手机号、验证码、新密码、确认密码）
     * @return 是否重置成功
     */
    boolean resetPassword(ResetPasswordDTO resetPasswordDTO);

    // ==================== 管理端接口 ====================

    /**
     * 用户分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<UserVO> getUserPage(UserQueryDTO queryDTO);

    /**
     * 用户详情（管理端）
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserDetail(Long userId);

    /**
     * 新增用户（管理员操作）
     *
     * @param userAddDTO 新增参数
     * @return 是否成功
     */
    boolean addUser(UserAddDTO userAddDTO);

    /**
     * 修改用户（管理员操作）
     *
     * @param userUpdateDTO 修改参数
     * @return 是否成功
     */
    boolean updateUser(UserUpdateDTO userUpdateDTO);

    /**
     * 删除用户（逻辑删除）
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);

    /**
     * 重置用户密码（管理员操作）
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean resetUserPassword(Long userId);

    /**
     * 分配用户角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean assignRoles(Long userId, java.util.List<Long> roleIds);
}
