package com.pixeltribe.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
@EnableCaching
class RedisConfig {
	
	@Bean    // Redis 連線設定
	public RedisConnectionFactory redisConnectionFactory() {
		return null; // 暫定，未來會修改
	}
	
	@Bean   // Redis 操作模板
	public RedisTemplate<String, Object> redisTemplate() {
		return null;  // 暫定，未來會修改
    }
	
	@Bean  // 快取管理器
    public CacheManager cacheManager() {
		return null;  // 暫定，未來會修改
    }
	
	
}