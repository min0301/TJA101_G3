package com.pixeltribe.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@EnableRedisRepositories
@EnableCaching
class RedisConfig {

    @Bean    // Redis 連線設定
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory("localhost", 6379);
        return factory;
    }

    @Bean   // Redis 操作模板 (最終修正版)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // --- Key 的序列化方式 ---
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // --- Value 的序列化方式 ---

        // 1. 建立一個 ObjectMapper，這是 Jackson 的核心
        ObjectMapper objectMapper = new ObjectMapper();

        // 2. 註冊 JavaTimeModule 模組，讓 ObjectMapper 認識 java.time.*
        objectMapper.registerModule(new JavaTimeModule());

        // 3. (建議) 關閉將日期寫入為時間戳的預設行為，讓輸出的 JSON 更具可讀性
        // 例如，輸出 "2025-07-10T21:06:30.438Z" 而不是一串數字
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 4. (重要) 設定預設的型別資訊，這能讓 Redis 在反序列化時知道要轉成哪個具體類別
        // 這一步是 GenericJackson2JsonRedisSerializer 預設會做的事，我們手動配置時要補上
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 5. 用我們配置好的 ObjectMapper 建立 JSON 序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

}