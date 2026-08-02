package com.haikou.government.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.common.core.utils.JwtUtils;
import com.haikou.government.common.core.utils.HttpRequestHolder;
import com.haikou.government.common.redis.utils.RedisUtils;
import com.haikou.government.common.security.utils.PasswordUtils;
import com.haikou.government.system.domain.SysUser;
import com.haikou.government.system.domain.SysLoginLog;
import com.haikou.government.system.dto.LoginDTO;
import com.haikou.government.system.dto.RealNameDTO;
import com.haikou.government.system.dto.RegisterDTO;
import com.haikou.government.system.dto.SmsLoginDTO;
import com.haikou.government.system.vo.LoginVO;
import com.haikou.government.system.vo.RealNameVO;
import com.haikou.government.system.mapper.SysUserMapper;
import com.haikou.government.system.mapper.SysLoginLogMapper;
import com.haikou.government.system.service.SmsService;
import com.haikou.government.system.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
     * 密码登录
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
}
