package com.haikou.government.system.service;

/**
 * 短信验证码服务
 *
 * @author xhl
 */
public interface SmsService {

    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendCode(String phone);

    /**
     * 校验验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否正确
     */
    boolean verifyCode(String phone, String code);
}
