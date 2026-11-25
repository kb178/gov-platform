package com.haikou.government.common.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson 全局序列化配置
 *
 * 把 Long 类型序列化成字符串，防止 JS 精度丢失。
 * 雪花算法生成的 ID 超过 JS Number.MAX_SAFE_INTEGER（2^53-1），
 * JSON 里传数字会丢精度，导致前端拿 ID 查后端查不到。
 *
 * @author xhl
 * @since 2026-11-25
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        // 用 builder 创建，保留 Spring Boot 默认配置（LocalDateTime 等时间类型的序列化）
        ObjectMapper mapper = builder.createXmlMapper(false).build();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        mapper.registerModule(module);  // 再额外装一个 Long 转 String 的
        return mapper;
    }
}
