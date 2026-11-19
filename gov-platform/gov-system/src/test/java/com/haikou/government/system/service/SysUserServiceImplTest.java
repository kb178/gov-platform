package com.haikou.government.system.service;

import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.common.core.utils.JwtUtils;
import com.haikou.government.common.redis.utils.RedisUtils;
import com.haikou.government.system.domain.SysLoginLog;
import com.haikou.government.system.domain.SysUser;
import com.haikou.government.system.dto.LoginDTO;
import com.haikou.government.system.dto.RegisterDTO;
import com.haikou.government.system.mapper.SysLoginLogMapper;
import com.haikou.government.system.mapper.SysRoleMapper;
import com.haikou.government.system.mapper.SysUserMapper;
import com.haikou.government.system.mapper.SysUserRoleMapper;
import com.haikou.government.system.service.impl.SysUserServiceImpl;
import com.haikou.government.system.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysUserServiceImpl 单元测试
 *
 * 测试策略：
 * - 用 Mockito 做纯单元测试，不起 Spring 容器，不连数据库/Redis
 * - SysUserServiceImpl extends ServiceImpl，save()/updateById() 内部走 baseMapper
 *   所以 mock baseMapper.insert() / baseMapper.updateById() 来控制行为
 * - 重点覆盖业务规则的正常流程和异常分支
 *
 * @author gov-platform
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
class SysUserServiceImplTest {

    /** 被测对象，Mockito 会把 @Mock 的对象注入到它的字段里（包括父类 ServiceImpl 的 baseMapper） */
    @InjectMocks
    private SysUserServiceImpl sysUserService;

    @Mock
    private SmsService smsService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RedisUtils redisUtils;

    /** ServiceImpl 父类中的 baseMapper，Mockito 会注入到继承的 protected 字段 */
    @Mock
    private SysUserMapper baseMapper;

    @Mock
    private SysLoginLogMapper loginLogMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @Mock
    private SysRoleMapper roleMapper;

    /**
     * baseMapper 是 ServiceImpl 父类的 protected 字段，
     * Mockito 的 @InjectMocks 有时注入不进去，手动用反射设置
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sysUserService, "baseMapper", baseMapper);
    }

    // ==================== 测试数据 ====================

    private SysUser buildUser() {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setUsername("admin");
        // BCrypt hash of "admin123"
        user.setPassword("$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2");
        user.setNickname("超级管理员");
        user.setPhone("13800000000");
        user.setUserType((byte) 2);
        user.setStatus((byte) 0);
        return user;
    }

    private LoginDTO buildLoginDTO() {
        LoginDTO dto = new LoginDTO();
        dto.setPhone("13800000000");
        dto.setPassword("admin123");
        return dto;
    }

    private RegisterDTO buildRegisterDTO() {
        RegisterDTO dto = new RegisterDTO();
        dto.setPhone("13800000001");
        dto.setCode("123456");
        dto.setPassword("test123");
        dto.setConfirmPassword("test123");
        return dto;
    }

    // ==================== 注册 ====================

    @Test
    @DisplayName("注册成功 - 手机号未注册，验证码正确")
    void register_success() {
        RegisterDTO dto = buildRegisterDTO();

        when(smsService.verifyCode("13800000001", "123456")).thenReturn(true);
        when(baseMapper.selectCount(any())).thenReturn(0L);
        // save() 内部调 baseMapper.insert()，返回 1 表示成功
        when(baseMapper.insert(any(SysUser.class))).thenReturn(1);

        boolean result = sysUserService.register(dto);

        assertTrue(result);
        verify(smsService).verifyCode("13800000001", "123456");
        verify(baseMapper).insert(any(SysUser.class));
    }

    @Test
    @DisplayName("注册失败 - 两次密码不一致")
    void register_passwordMismatch() {
        RegisterDTO dto = buildRegisterDTO();
        dto.setConfirmPassword("different");

        when(smsService.verifyCode(anyString(), anyString())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.register(dto));
        assertEquals("两次密码不一致", ex.getMessage());
        // 不应该走到查库和插入
        verify(baseMapper, never()).selectCount(any());
        verify(baseMapper, never()).insert(any(SysUser.class));
    }

    @Test
    @DisplayName("注册失败 - 手机号已被注册")
    void register_phoneAlreadyExists() {
        RegisterDTO dto = buildRegisterDTO();

        when(smsService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(baseMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.register(dto));
        assertEquals("该手机号已注册", ex.getMessage());
        verify(baseMapper, never()).insert(any(SysUser.class));
    }

    // ==================== 登录 ====================

    @Test
    @DisplayName("登录成功 - 手机号密码正确")
    void login_success() {
        LoginDTO dto = buildLoginDTO();
        SysUser user = buildUser();

        when(redisUtils.get(anyString())).thenReturn(null); // 没有失败记录
        when(baseMapper.selectOne(any())).thenReturn(user);
        when(jwtUtils.createToken(anyLong(), anyString(), anyString())).thenReturn("mock-jwt-token");
        when(loginLogMapper.insert(any(SysLoginLog.class))).thenReturn(1);

        LoginVO vo = sysUserService.login(dto);

        assertNotNull(vo);
        assertEquals("mock-jwt-token", vo.getAccessToken());
        assertEquals(1L, vo.getUserId());
        assertEquals("admin", vo.getUsername());
        // 登录成功后清除失败计数
        verify(redisUtils).delete(anyString());
    }

    @Test
    @DisplayName("登录失败 - 连续失败5次被锁定")
    void login_lockedAfter5Failures() {
        LoginDTO dto = buildLoginDTO();

        // 已经失败了5次
        when(redisUtils.get(anyString())).thenReturn(5);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.login(dto));
        assertTrue(ex.getMessage().contains("登录失败次数过多"));
        // 被锁定时不应该查库
        verify(baseMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("登录失败 - 手机号未注册")
    void login_userNotFound() {
        LoginDTO dto = buildLoginDTO();

        when(redisUtils.get(anyString())).thenReturn(null);
        when(baseMapper.selectOne(any())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.login(dto));
        assertEquals("手机号或密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_wrongPassword() {
        LoginDTO dto = buildLoginDTO();
        dto.setPassword("wrongpassword");
        SysUser user = buildUser();

        when(redisUtils.get(anyString())).thenReturn(null);
        when(baseMapper.selectOne(any())).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.login(dto));
        assertEquals("手机号或密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 账号已停用")
    void login_accountDisabled() {
        LoginDTO dto = buildLoginDTO();
        SysUser user = buildUser();
        user.setStatus((byte) 1);

        when(redisUtils.get(anyString())).thenReturn(null);
        when(baseMapper.selectOne(any())).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.login(dto));
        assertEquals("账号已被停用，请联系管理员", ex.getMessage());
    }
}
