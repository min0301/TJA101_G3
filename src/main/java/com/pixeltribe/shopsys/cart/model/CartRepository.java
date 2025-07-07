package com.pixeltribe.shopsys.cart.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
	private String generateKey(Integer memNo) {
		return CART_KEY_PREFIX + memNo;
	}
	
	
	// ========== 保存購物車到Redis裡 ========== //
	public void saveCart(Integer memNo, CartDTO cart) {
		try {
			String key = generateKey(memNo);
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
	public CartDTO getCart(Integer memNo) {
		try {
			String key = generateKey(memNo);
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
	public void deleteCart(Integer memNo) {
		String key = generateKey(memNo);
        Boolean deleted = redisTemplate.delete(key);
        
        if (Boolean.TRUE.equals(deleted)) {
        	System.out.println("購物車已從Redis刪除: " + key);
        } else {
        	System.out.println("Redis中沒有找到要刪除的購物車: " + key);
        }
	}
	
	
	// ******** 後台 ******** //
			// ========== 查詢所有購物車 ========== //
		    public List<CartDTO> getAllCarts(Integer page, Integer size) {
		    	try {
		            List<CartDTO> carts = new ArrayList<>();
		            
		            // 查詢所有符合前綴的 key
		            Set<String> keys = redisTemplate.keys(CART_KEY_PREFIX + "*");
		            
		            if (keys == null || keys.isEmpty()) {
		                return carts; // 回傳空清單
		            }
		            
		            // 計算分頁
		            List<String> keyList = new ArrayList<>(keys);
		            int startIndex = (page - 1) * size;
		            int endIndex = Math.min(startIndex + size, keyList.size());
		            
		            // 取得分頁範圍內的 key
		            for (int i = startIndex; i < endIndex; i++) {
		                String key = keyList.get(i);
		                String jsonValue = redisTemplate.opsForValue().get(key);
		                
		                if (jsonValue != null) {
		                    try {
		                        CartDTO cart = objectMapper.readValue(jsonValue, CartDTO.class);
		                        carts.add(cart);
		                    } catch (Exception e) {
		                        System.err.println("解析購物車資料失敗: " + key);
		                    }
		                }
		            }
		            
		            return carts;
		            
		        } catch (Exception e) {
		            System.err.println("查詢所有購物車失敗: " + e.getMessage());
		            return new ArrayList<>(); // 回傳空清單
		        }
		    }
		    
		    
		    // ========== 統計購物車總數 ========== //
		    public Integer getTotalCartsCount() {
		        try {
		            Set<String> keys = redisTemplate.keys(CART_KEY_PREFIX + "*");
		            return keys != null ? keys.size() : 0;
		        } catch (Exception e) {
		            System.err.println("統計購物車總數失敗: " + e.getMessage());
		            return 0;
		        }
		    }    
	
}