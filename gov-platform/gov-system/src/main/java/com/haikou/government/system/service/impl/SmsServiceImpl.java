package com.haikou.government.system.service.impl;

import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.common.redis.utils.RedisUtils;
import com.haikou.government.system.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务实现类
 *
 * @author xhl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final RedisUtils redisUtils;

    /**
     * 验证码在 Redis 中的 key 前缀
     */
    private static final String CODE_PREFIX = "sms:code:";

    /**
     * 验证码有效期（分钟）
     */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /**
     * 限制发送间隔（秒）
     */
    private static final int SEND_INTERVAL_SECONDS = 60;

    @Override
    public boolean sendCode(String phone) {
        // 1. 检查是否频繁发送（60秒内只能发一次）
        String intervalKey = "sms:interval:" + phone;
        if (redisUtils.hasKey(intervalKey)) {
            throw new BusinessException("发送太频繁，请60秒后再试");
        }

        // 2. 生成6位随机验证码
        String code = generateCode();

        // 3. 存储到 Redis（5分钟有效）
        String codeKey = CODE_PREFIX + phone;
        redisUtils.set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 4. 设置发送间隔限制
        redisUtils.set(intervalKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 5. 调用短信发送（暂时模拟，打印日志）
        // TODO: 接入真实短信服务（阿里云/腾讯云）
        log.info("【短信验证码】手机号：{}，验证码：{}", phone, code);

        return true;
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        // 1. 从 Redis 获取验证码
        String codeKey = CODE_PREFIX + phone;
        String cachedCode = redisUtils.get(codeKey);

        // 2. 校验验证码
        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(code)) {
            throw new BusinessException("验证码错误");
        }

        // 3. 验证成功后删除验证码（一次性使用）
        redisUtils.delete(codeKey);

        return true;
    }

    /**
     * 生成6位随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
