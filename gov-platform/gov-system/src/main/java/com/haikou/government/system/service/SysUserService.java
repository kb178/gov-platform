package com.haikou.government.system.service;

import com.haikou.government.system.domain.SysUser;
import com.haikou.government.system.dto.LoginDTO;
import com.haikou.government.system.dto.RegisterDTO;
import com.haikou.government.system.dto.SmsLoginDTO;
import com.haikou.government.system.vo.LoginVO;
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
}
