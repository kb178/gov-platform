package com.haikou.government.common.redis.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 *
 * @author gov-platform
 */
@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    // ============================ String 操作 ============================

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存（带过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置缓存（带过期时间，单位秒）
     */
    public void set(String key, Object value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 批量获取
     */
    public List<Object> multiGet(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 仅当 key 不存在时设置（分布式锁常用）
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 判断 key 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间（秒）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 自增
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 自增指定步长
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ============================ Hash 操作 ============================

    /**
     * 设置 Hash
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 获取 Hash
     */
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String field) {
        return (T) redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 获取 Hash 所有值
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除 Hash 字段
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 判断 Hash 字段是否存在
     */
    public Boolean hHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    // ============================ List 操作 ============================

    /**
     * 左推入
     */
    public Long lLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 右推入
     */
    public Long lRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 获取 List 范围
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取 List 长度
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // ============================ Set 操作 ============================

    /**
     * 添加 Set
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 获取 Set 所有成员
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断是否是 Set 成员
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取 Set 长度
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // ============================ Token 黑名单 ============================

    /** Token 黑名单 key 前缀 */
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * 将 Token 加入黑名单（主动失效）
     *
     * 使用场景：退出登录、修改密码、账号被封禁时调用
     * TTL 设为 Token 剩余有效期，到期后自动从 Redis 清除（此时 Token 本身也过期了）
     *
     * @param token            JWT Token 字符串
     * @param remainingSeconds Token 剩余有效期（秒）
     */
    public void addToTokenBlacklist(String token, long remainingSeconds) {
        if (token == null || remainingSeconds <= 0) {
            return;
        }
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", remainingSeconds, TimeUnit.SECONDS);
    }

    /**
     * 判断 Token 是否在黑名单中
     *
     * @param token JWT Token 字符串
     * @return true=已拉黑（应拒绝），false=正常
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null) {
            return false;
        }
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 将 Token 从黑名单中移除（一般不需要调用，TTL 会自动清理）
     *
     * @param token JWT Token 字符串
     */
    public void removeFromTokenBlacklist(String token) {
        if (token == null) {
            return;
        }
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
    }
}
