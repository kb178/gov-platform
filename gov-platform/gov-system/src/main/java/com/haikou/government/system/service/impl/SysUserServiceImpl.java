package com.haikou.government.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.common.security.utils.PasswordUtils;
import com.haikou.government.system.domain.SysUser;
import com.haikou.government.system.domain.dto.RegisterDTO;
import com.haikou.government.system.mapper.SysUserMapper;
import com.haikou.government.system.service.SmsService;
import com.haikou.government.system.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author xhl
 * @since 2026-08-01
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SmsService smsService;

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
        user.setUsername(registerDTO.getPhone()); // 默认用手机号作为用户名
        user.setPassword(PasswordUtils.encode(registerDTO.getPassword())); // 使用工具类加密
        user.setUserType((byte) 1); // 1=老百姓
        user.setStatus((byte) 0); // 0=正常

        // 5. 插入数据库
        return save(user);
    }
}
