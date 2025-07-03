package com.pixeltribe.shopsys.cart.model;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class CartRepository {
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	// Redis Key 前綴
	private static final String CART_KEY_PREFIX = "cart:member:";
	
	// 購物車過期時間(半年)
	private static final Duration CART_EXPIRE_TIME = Duration.ofDays(365 / 2);
	
	// ========== 生成 Redis Key ========== //
	private String generateKey(Integer id) {
		return CART_KEY_PREFIX + id;
	}
	
	
	// ========== 保存購物車到Redis裡 ========== //
	public void saveCart(Integer id, CartDTO cart) {
		try {
			String key = generateKey(id);
			String jsonValue = objectMapper.writeValueAsString(cart);
			
			// 保存並設定過期時間
            redisTemplate.opsForValue().set(key, jsonValue, CART_EXPIRE_TIME);
            System.out.println("購物車已保存到Redis: " + key);
			
		} catch (Exception e) {
			System.err.println("保存購物車到Redis失敗:" + e.getMessage());
			throw new RuntimeException("保存購物車失敗", e);
		}
	}
	
	
	// ========== 從Redis獲取購物車資訊 ========== //
	public CartDTO getCart(Integer id) {
		try {
			String key = generateKey(id);
            String jsonValue = redisTemplate.opsForValue().get(key);
            
            if (jsonValue == null) {
            	System.out.println("Redis中沒有找到購物車: " + key);
                return null; // 購物車不存在
            }
			
            CartDTO cart = objectMapper.readValue(jsonValue, CartDTO.class);
            
         // 延長過期時間
            redisTemplate.expire(key, CART_EXPIRE_TIME);
            
            System.out.println("從Redis取得購物車: " + key);
            return cart;
		} catch (Exception e) {
			System.err.println("從Redis讀取購物車失敗: " + e.getMessage());
            return null; // 讀取失敗，回傳 null
		}
	}
	
	
	// ========== 刪除購物車 ========== //
	public void deleteCart(Integer id) {
		String key = generateKey(id);
        Boolean deleted = redisTemplate.delete(key);
        
        if (Boolean.TRUE.equals(deleted)) {
        	System.out.println("購物車已從Redis刪除: " + key);
        } else {
        	System.out.println("Redis中沒有找到要刪除的購物車: " + key);
        }
	}
}