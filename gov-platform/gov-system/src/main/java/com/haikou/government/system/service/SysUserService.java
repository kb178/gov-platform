package com.haikou.government.system.service;

import com.haikou.government.system.domain.SysUser;
import com.haikou.government.system.domain.dto.RegisterDTO;
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
}
