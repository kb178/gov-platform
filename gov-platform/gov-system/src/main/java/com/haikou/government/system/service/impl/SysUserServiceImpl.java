package com.haikou.government.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.common.core.domain.PageResult;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.common.core.utils.JwtUtils;
import com.haikou.government.common.core.utils.HttpRequestHolder;
import com.haikou.government.common.redis.utils.RedisUtils;
import com.haikou.government.common.security.utils.PasswordUtils;
import com.haikou.government.system.domain.SysUser;
import com.haikou.government.system.domain.SysLoginLog;
import com.haikou.government.system.domain.SysUserRole;
import com.haikou.government.system.dto.*;
import com.haikou.government.system.vo.LoginVO;
import com.haikou.government.system.vo.RealNameVO;
import com.haikou.government.system.vo.UserInfoVO;
import com.haikou.government.system.vo.UserVO;
import com.haikou.government.system.mapper.SysUserMapper;
import com.haikou.government.system.mapper.SysLoginLogMapper;
import com.haikou.government.system.mapper.SysUserRoleMapper;
import com.haikou.government.system.mapper.SysRoleMapper;
import com.haikou.government.system.service.SmsService;
import com.haikou.government.system.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author xhl
 * @since 2026-08-01
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SmsService smsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private SysLoginLogMapper loginLogMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    /** 登录失败次数 Redis Key 前缀 */
    private static final String LOGIN_FAIL_KEY = "login:fail:";

    /** 最大登录失败次数 */
    private static final int MAX_FAIL_COUNT = 5;

    /** 登录失败锁定时间（分钟） */
    private static final int LOCK_MINUTES = 15;

    /**
     * 用户注册
     */
    @Override
    public boolean register(RegisterDTO registerDTO) {
        // 1. 校验验证码
        smsService.verifyCode(registerDTO.getPhone(), registerDTO.getCode());

        // 2. 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }

        // 3. 校验手机号是否已注册
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getPhone, registerDTO.getPhone());
        Long count = baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("该手机号已注册");
        }

        // 4. 创建用户对象
        SysUser user = new SysUser();
        user.setPhone(registerDTO.getPhone());
        user.setUsername(registerDTO.getPhone());
        user.setPassword(PasswordUtils.encode(registerDTO.getPassword()));
        user.setUserType((byte) 1);
        user.setStatus((byte) 0);

        // 5. 插入数据库
        return save(user);
    }

    /**
     * 密码登录（政务端 - 手机号+密码）
     *
     * @param loginDTO 登录参数（手机号 + 密码）
     * @return LoginVO 登录成功信息
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String phone = loginDTO.getPhone();
        String failKey = LOGIN_FAIL_KEY + phone;

        // ========== 第一步：从 ThreadLocal 获取客户端IP ==========
        String loginIp = HttpRequestHolder.getClientIp();

        // ========== 第二步：检查登录失败锁定 ==========
        Integer failCount = redisUtils.get(failKey);
        if (failCount != null && failCount >= MAX_FAIL_COUNT) {
            log.warn("登录失败：手机号 {} 已被锁定，失败次数 {}", maskPhone(phone), failCount);
            throw new BusinessException("登录失败次数过多，请" + LOCK_MINUTES + "分钟后再试");
        }

        // ========== 第三步：根据手机号查询用户 ==========
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getPhone, phone);
        SysUser user = baseMapper.selectOne(queryWrapper);

        // ========== 第四步：校验用户是否存在 ==========
        if (user == null) {
            incrementFailCount(failKey);
            saveLoginLog(phone, loginIp, false, "手机号未注册");
            log.warn("登录失败：手机号 {} 未注册，IP {}", maskPhone(phone), loginIp);
            throw new BusinessException("手机号或密码错误");
        }

        // ========== 第五步：校验密码是否正确 ==========
        if (!PasswordUtils.matches(loginDTO.getPassword(), user.getPassword())) {
            incrementFailCount(failKey);
            saveLoginLog(phone, loginIp, false, "密码错误");
            log.warn("登录失败：用户 {} 密码错误，IP {}", maskPhone(phone), loginIp);
            throw new BusinessException("手机号或密码错误");
        }

        // ========== 第六步：校验账号状态 ==========
        if (user.getStatus() != null && user.getStatus() == 1) {
            saveLoginLog(phone, loginIp, false, "账号已停用");
            log.warn("登录失败：用户 {} 已被停用，IP {}", maskPhone(phone), loginIp);
            throw new BusinessException("账号已被停用，请联系管理员");
        }

        // ========== 第七步：登录成功，清除失败次数 ==========
        redisUtils.delete(failKey);

        // ========== 第八步：生成 JWT Token ==========
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        String token = jwtUtils.createToken(user.getUserId(), user.getUsername(), uuid);

        // ========== 第九步：记录登录日志 ==========
        saveLoginLog(phone, loginIp, true, "登录成功");
        log.info("用户 {} 登录成功，IP {}", maskPhone(phone), loginIp);

        // ========== 第十步：构建返回对象 ==========
        return LoginVO.builder()
                .accessToken(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .userType(user.getUserType())
                .build();
    }

    /**
     * 验证码登录
     *
     * @param smsLoginDTO 登录参数（手机号 + 验证码）
     * @return LoginVO 登录成功信息
     */
    @Override
    public LoginVO smsLogin(SmsLoginDTO smsLoginDTO) {
        String phone = smsLoginDTO.getPhone();

        // ========== 第一步：从 ThreadLocal 获取客户端IP ==========
        String loginIp = HttpRequestHolder.getClientIp();

        // ========== 第二步：校验验证码 ==========
        smsService.verifyCode(phone, smsLoginDTO.getCode());

        // ========== 第三步：根据手机号查询用户 ==========
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getPhone, phone);
        SysUser user = baseMapper.selectOne(queryWrapper);

        // ========== 第四步：用户不存在则自动注册 ==========
        if (user == null) {
            user = new SysUser();
            user.setPhone(phone);
            user.setUsername(phone);
            user.setPassword(PasswordUtils.encode("")); // 验证码登录用户无密码
            user.setUserType((byte) 1);
            user.setStatus((byte) 0);
            save(user);
            log.info("用户 {} 验证码登录自动注册", maskPhone(phone));
        }

        // ========== 第五步：校验账号状态 ==========
        if (user.getStatus() != null && user.getStatus() == 1) {
            saveLoginLog(phone, loginIp, false, "账号已停用");
            log.warn("登录失败：用户 {} 已被停用，IP {}", maskPhone(phone), loginIp);
            throw new BusinessException("账号已被停用，请联系管理员");
        }

        // ========== 第六步：生成 JWT Token ==========
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        String token = jwtUtils.createToken(user.getUserId(), user.getUsername(), uuid);

        // ========== 第七步：记录登录日志 ==========
        saveLoginLog(phone, loginIp, true, "验证码登录成功");
        log.info("用户 {} 验证码登录成功，IP {}", maskPhone(phone), loginIp);

        // ========== 第八步：构建返回对象 ==========
        return LoginVO.builder()
                .accessToken(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .userType(user.getUserType())
                .build();
    }

    /**
     * 管理员登录（用户名+密码）
     *
     * @param adminLoginDTO 登录参数（用户名 + 密码）
     * @return LoginVO 登录成功信息
     */
    @Override
    public LoginVO adminLogin(AdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String failKey = LOGIN_FAIL_KEY + "admin:" + username;

        // ========== 第一步：从 ThreadLocal 获取客户端IP ==========
        String loginIp = HttpRequestHolder.getClientIp();

        // ========== 第二步：检查登录失败锁定 ==========
        Integer failCount = redisUtils.get(failKey);
        if (failCount != null && failCount >= MAX_FAIL_COUNT) {
            log.warn("管理员登录失败：用户名 {} 已被锁定，失败次数 {}", username, failCount);
            throw new BusinessException("登录失败次数过多，请" + LOCK_MINUTES + "分钟后再试");
        }

        // ========== 第三步：根据用户名查询用户 ==========
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        SysUser user = baseMapper.selectOne(queryWrapper);

        // ========== 第四步：校验用户是否存在 ==========
        if (user == null) {
            incrementFailCount(failKey);
            saveLoginLog(username, loginIp, false, "用户名不存在");
            log.warn("管理员登录失败：用户名 {} 不存在，IP {}", username, loginIp);
            throw new BusinessException("用户名或密码错误");
        }

        // ========== 第五步：校验密码是否正确 ==========
        if (!PasswordUtils.matches(adminLoginDTO.getPassword(), user.getPassword())) {
            incrementFailCount(failKey);
            saveLoginLog(username, loginIp, false, "密码错误");
            log.warn("管理员登录失败：用户 {} 密码错误，IP {}", username, loginIp);
            throw new BusinessException("用户名或密码错误");
        }

        // ========== 第六步：校验账号状态 ==========
        if (user.getStatus() != null && user.getStatus() == 1) {
            saveLoginLog(username, loginIp, false, "账号已停用");
            log.warn("管理员登录失败：用户 {} 已被停用，IP {}", username, loginIp);
            throw new BusinessException("账号已被停用，请联系超级管理员");
        }

        // ========== 第七步：校验用户类型（必须是工作人员） ==========
        if (user.getUserType() == null || user.getUserType() != 2) {
            saveLoginLog(username, loginIp, false, "非工作人员账号");
            log.warn("管理员登录失败：用户 {} 不是工作人员，IP {}", username, loginIp);
            throw new BusinessException("该账号无管理权限");
        }

        // ========== 第八步：登录成功，清除失败次数 ==========
        redisUtils.delete(failKey);

        // ========== 第九步：生成 JWT Token ==========
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        String token = jwtUtils.createToken(user.getUserId(), user.getUsername(), uuid);

        // ========== 第十步：记录登录日志 ==========
        saveLoginLog(username, loginIp, true, "管理员登录成功");
        log.info("管理员 {} 登录成功，IP {}", username, loginIp);

        // ========== 第十一步：构建返回对象 ==========
        return LoginVO.builder()
                .accessToken(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .userType(user.getUserType())
                .build();
    }

    /**
     * 增加登录失败次数
     */
    private void incrementFailCount(String failKey) {
        Integer count = redisUtils.get(failKey);
        if (count == null) {
            redisUtils.set(failKey, 1, LOCK_MINUTES, TimeUnit.MINUTES);
        } else {
            redisUtils.set(failKey, count + 1, LOCK_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * 保存登录日志
     *
     * @param phone    手机号
     * @param ip       登录IP
     * @param success  是否成功
     * @param msg      提示消息
     */
    private void saveLoginLog(String phone, String ip, boolean success, String msg) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUsername(phone);
            loginLog.setIpaddr(ip);
            loginLog.setStatus(success ? (byte) 0 : (byte) 1);
            loginLog.setMsg(msg);
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            // 日志保存失败不影响登录流程
            log.error("保存登录日志失败：{}", e.getMessage());
        }
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 实名认证
     *
     * @param userId 用户ID
     * @param realNameDTO 实名认证参数（姓名 + 身份证号）
     * @return 是否认证成功
     */
    @Override
    public boolean realNameAuth(Long userId, RealNameDTO realNameDTO) {
        // 1. 查询用户
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 检查是否已认证
        if (user.getRealNameStatus() != null && user.getRealNameStatus() == 1) {
            throw new BusinessException("已实名认证，请勿重复认证");
        }

        // 3. 校验身份证号是否已被其他用户使用
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getIdCard, realNameDTO.getIdCard())
                    .ne(SysUser::getUserId, userId);
        Long count = baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("该身份证号已被其他用户认证");
        }

        // 4. 更新用户实名信息
        user.setRealName(realNameDTO.getRealName());
        user.setIdCard(realNameDTO.getIdCard());
        user.setRealNameStatus((byte) 1);
        user.setVerifyTime(LocalDateTime.now());

        // 5. 保存到数据库
        boolean result = updateById(user);
        if (result) {
            log.info("用户 {} 实名认证成功，身份证号：{}", userId, maskIdCard(realNameDTO.getIdCard()));
        }
        return result;
    }

    /**
     * 查询实名认证状态
     *
     * @param userId 用户ID
     * @return 实名认证信息
     */
    @Override
    public RealNameVO getRealNameStatus(Long userId) {
        // 1. 查询用户
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 构建返回结果
        return RealNameVO.builder()
                .status(user.getRealNameStatus() != null ? user.getRealNameStatus() : (byte) 0)
                .realName(maskRealName(user.getRealName()))
                .idCard(maskIdCard(user.getIdCard()))
                .verifyTime(user.getVerifyTime() != null ? user.getVerifyTime().toString().replace("T", " ") : null)
                .build();
    }

    /**
     * 真实姓名脱敏
     * 示例：张三 → 张*三，张小三 → 张*三
     */
    private String maskRealName(String realName) {
        if (realName == null || realName.length() < 2) {
            return "***";
        }
        if (realName.length() == 2) {
            return realName.charAt(0) + "*";
        }
        return realName.charAt(0) + "*" + realName.charAt(realName.length() - 1);
    }

    /**
     * 身份证号脱敏
     * 示例：460100199001011234 → 460100********1234
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return "***";
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param changePasswordDTO 修改密码参数
     * @return 是否修改成功
     */
    @Override
    public boolean changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        // 1. 验证新密码和确认密码是否一致
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BusinessException("新密码和确认密码不一致");
        }

        // 2. 查询用户
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 验证旧密码是否正确
        if (!PasswordUtils.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 4. 验证新密码不能和旧密码相同
        if (changePasswordDTO.getOldPassword().equals(changePasswordDTO.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 5. 更新密码（BCrypt 加密）
        user.setPassword(PasswordUtils.encode(changePasswordDTO.getNewPassword()));
        boolean success = updateById(user);

        if (success) {
            log.info("用户修改密码成功，userId={}", userId);
        }

        return success;
    }

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public UserInfoVO getUserInfo(Long userId) {
        // 1. 查询用户
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 构建返回结果
        return UserInfoVO.builder()
                .userId(user.getUserId())
                .phone(maskPhone(user.getPhone()))  // 返回脱敏手机号
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .sex(user.getSex())
                .email(user.getEmail())
                .realNameStatus(user.getRealNameStatus() != null ? user.getRealNameStatus() : (byte) 0)
                .realName(maskRealName(user.getRealName()))
                .verifyTime(user.getVerifyTime() != null ? user.getVerifyTime().toString().replace("T", " ") : null)
                .build();
    }

    /**
     * 修改个人信息
     *
     * @param userId 用户ID
     * @param updateUserDTO 修改参数
     * @return 是否修改成功
     */
    @Override
    public boolean updateUserInfo(Long userId, UpdateUserDTO updateUserDTO) {
        // 1. 查询用户
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 更新字段（只更新非空字段）
        if (updateUserDTO.getNickname() != null) {
            user.setNickname(updateUserDTO.getNickname());
        }
        if (updateUserDTO.getAvatar() != null) {
            user.setAvatar(updateUserDTO.getAvatar());
        }
        if (updateUserDTO.getSex() != null) {
            user.setSex(updateUserDTO.getSex());
        }
        if (updateUserDTO.getEmail() != null) {
            user.setEmail(updateUserDTO.getEmail());
        }

        // 3. 保存到数据库
        boolean success = updateById(user);
        if (success) {
            log.info("用户修改个人信息成功，userId={}", userId);
        }

        return success;
    }

    /**
     * 找回密码（重置密码）
     *
     * @param resetPasswordDTO 重置密码参数
     * @return 是否重置成功
     */
    @Override
    public boolean resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // 1. 验证新密码和确认密码是否一致
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            throw new BusinessException("新密码和确认密码不一致");
        }

        // 2. 验证验证码
        String cacheCode = redisUtils.get("sms:code:" + resetPasswordDTO.getPhone());
        if (cacheCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!cacheCode.equals(resetPasswordDTO.getCode())) {
            throw new BusinessException("验证码错误");
        }

        // 3. 查询用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getPhone, resetPasswordDTO.getPhone());
        SysUser user = baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("该手机号未注册");
        }

        // 4. 更新密码（BCrypt 加密）
        user.setPassword(PasswordUtils.encode(resetPasswordDTO.getNewPassword()));
        boolean success = updateById(user);

        // 5. 删除验证码
        if (success) {
            redisUtils.delete("sms:code:" + resetPasswordDTO.getPhone());
            log.info("用户重置密码成功，phone={}", maskPhone(resetPasswordDTO.getPhone()));
        }

        return success;
    }

    // ==================== 管理端接口实现 ====================

    /**
     * 用户分页列表
     */
    @Override
    public PageResult<UserVO> getUserPage(UserQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = buildUserQueryWrapper(queryDTO);

        // 2. 执行分页查询
        Page<SysUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<SysUser> userPage = baseMapper.selectPage(page, queryWrapper);

        // 3. 转换为 VO
        List<UserVO> voList = userPage.getRecords().stream()
                .map(this::convertToUserVO)
                .collect(Collectors.toList());

        // 4. 返回分页结果
        return new PageResult<>(userPage.getTotal(), voList, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /**
     * 用户详情（管理端）
     */
    @Override
    public UserVO getUserDetail(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserVO(user);
    }

    /**
     * 新增用户（管理员操作）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(UserAddDTO userAddDTO) {
        // 1. 校验用户名是否已存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, userAddDTO.getUsername());
        Long count = baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 校验手机号是否已存在
        if (StringUtils.hasText(userAddDTO.getPhone())) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getPhone, userAddDTO.getPhone());
            count = baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("手机号已被使用");
            }
        }

        // 3. 创建用户对象
        SysUser user = new SysUser();
        user.setUsername(userAddDTO.getUsername());
        user.setPassword(PasswordUtils.encode(userAddDTO.getPassword()));
        user.setNickname(userAddDTO.getNickname());
        user.setPhone(userAddDTO.getPhone());
        user.setEmail(userAddDTO.getEmail());
        user.setSex(userAddDTO.getSex());
        user.setUserType(userAddDTO.getUserType());
        user.setDeptId(userAddDTO.getDeptId());
        user.setRemark(userAddDTO.getRemark());
        user.setStatus((byte) 0);

        // 4. 保存用户
        boolean success = save(user);
        if (!success) {
            throw new BusinessException("新增用户失败");
        }

        // 5. 分配角色
        if (userAddDTO.getRoleIds() != null && !userAddDTO.getRoleIds().isEmpty()) {
            saveUserRoles(user.getUserId(), userAddDTO.getRoleIds());
        }

        log.info("新增用户成功：username={}", userAddDTO.getUsername());
        return true;
    }

    /**
     * 修改用户（管理员操作）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserUpdateDTO userUpdateDTO) {
        // 1. 查询用户
        SysUser user = getById(userUpdateDTO.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 校验手机号是否被其他用户使用
        if (StringUtils.hasText(userUpdateDTO.getPhone())) {
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getPhone, userUpdateDTO.getPhone())
                        .ne(SysUser::getUserId, userUpdateDTO.getUserId());
            Long count = baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("手机号已被其他用户使用");
            }
        }

        // 3. 更新用户信息
        if (userUpdateDTO.getNickname() != null) {
            user.setNickname(userUpdateDTO.getNickname());
        }
        if (userUpdateDTO.getPhone() != null) {
            user.setPhone(userUpdateDTO.getPhone());
        }
        if (userUpdateDTO.getEmail() != null) {
            user.setEmail(userUpdateDTO.getEmail());
        }
        if (userUpdateDTO.getSex() != null) {
            user.setSex(userUpdateDTO.getSex());
        }
        if (userUpdateDTO.getUserType() != null) {
            user.setUserType(userUpdateDTO.getUserType());
        }
        if (userUpdateDTO.getDeptId() != null) {
            user.setDeptId(userUpdateDTO.getDeptId());
        }
        if (userUpdateDTO.getStatus() != null) {
            user.setStatus(userUpdateDTO.getStatus());
        }
        if (userUpdateDTO.getRemark() != null) {
            user.setRemark(userUpdateDTO.getRemark());
        }

        // 4. 保存更新
        boolean success = updateById(user);
        if (success) {
            log.info("修改用户成功：userId={}", userUpdateDTO.getUserId());
        }
        return success;
    }

    /**
     * 删除用户（逻辑删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        // 1. 查询用户
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 删除用户（逻辑删除）
        boolean success = removeById(userId);
        if (success) {
            // 3. 删除用户角色关联
            LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserRole::getUserId, userId);
            userRoleMapper.delete(queryWrapper);

            log.info("删除用户成功：userId={}, username={}", userId, user.getUsername());
        }
        return success;
    }

    /**
     * 重置用户密码（管理员操作）
     */
    @Override
    public boolean resetUserPassword(Long userId) {
        // 1. 查询用户
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 重置为默认密码（123456）
        String defaultPassword = "123456";
        user.setPassword(PasswordUtils.encode(defaultPassword));

        // 3. 保存更新
        boolean success = updateById(user);
        if (success) {
            log.info("重置用户密码成功：userId={}", userId);
        }
        return success;
    }

    /**
     * 分配用户角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Long userId, List<Long> roleIds) {
        // 1. 校验用户是否存在
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 删除原有角色关联
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        userRoleMapper.delete(queryWrapper);

        // 3. 保存新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            saveUserRoles(userId, roleIds);
        }

        log.info("分配用户角色成功：userId={}, roleIds={}", userId, roleIds);
        return true;
    }

    // ==================== 私有方法 ====================

    /**
     * 构建用户查询条件
     */
    private LambdaQueryWrapper<SysUser> buildUserQueryWrapper(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getUsername())) {
            queryWrapper.like(SysUser::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getPhone())) {
            queryWrapper.like(SysUser::getPhone, queryDTO.getPhone());
        }
        if (queryDTO.getUserType() != null) {
            queryWrapper.eq(SysUser::getUserType, queryDTO.getUserType());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(SysUser::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getDeptId() != null) {
            queryWrapper.eq(SysUser::getDeptId, queryDTO.getDeptId());
        }

        queryWrapper.orderByDesc(SysUser::getCreateTime);
        return queryWrapper;
    }

    /**
     * 转换为 UserVO
     */
    private UserVO convertToUserVO(SysUser user) {
        UserVO vo = UserVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .realName(user.getRealName())
                .phone(maskPhone(user.getPhone()))
                .email(user.getEmail())
                .sex(user.getSex())
                .avatar(user.getAvatar())
                .userType(user.getUserType())
                .deptId(user.getDeptId())
                .status(user.getStatus())
                .realNameStatus(user.getRealNameStatus())
                .loginIp(user.getLoginIp())
                .loginTime(user.getLoginTime())
                .createTime(user.getCreateTime())
                .roleIds(new ArrayList<>())
                .roleNames(new ArrayList<>())
                .build();

        // 查询用户角色
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, user.getUserId());
        List<SysUserRole> userRoles = userRoleMapper.selectList(queryWrapper);
        if (userRoles != null && !userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());
            vo.setRoleIds(roleIds);
        }

        return vo;
    }

    /**
     * 保存用户角色关联
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }
}
