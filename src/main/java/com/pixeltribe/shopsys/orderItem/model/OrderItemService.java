package com.pixeltribe.shopsys.orderItem.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.stereotype.Service;

import com.pixeltribe.shopsys.cart.model.CartDTO.CartItem;
import com.pixeltribe.shopsys.product.model.ProductRepository;

@Service
public class OrderItemService {
	
	@Autowired
	private OrderItemRepository orderItemRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
//	@Autowired
//	private Redis RedisTemplate redisTemplate;
	
	
	// 查詢訂單項目（包含真實和假資資料(測試)）
    public List<OrderItem> getOrderItemsByOrderNo(Integer orderNo) {
        return orderItemRepository.findByOrderNoNative(orderNo);
    }
    
    // 便利方法：支援 String 參數
    public List<OrderItem> getOrderItemsByOrderNo(String orderNo) {
        try {
            Integer orderNoInt = Integer.valueOf(orderNo);
            return getOrderItemsByOrderNo(orderNoInt);
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }

    // 基本 CRUD 方法
    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }
    
    public OrderItem getOrderItemById(Integer orderItemNo) {
        return orderItemRepository.findById(orderItemNo).orElse(null);
    }
    
    public OrderItem saveOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }
    
    public void deleteOrderItem(Integer orderItemNo) {
        orderItemRepository.deleteById(orderItemNo);
    }
    
    
    //*********   下面先註解掉 未來再開 ******* //
    
//    // 從 Redis 購物車創建真實訂單項目
//    public void createOrderItemsFromCart(Integer orderNo, String memNo) {
//        // 1. 從 Redis 獲取購物車資料
//        List<CartItem> cartItems = getCartFromRedis(memNo);
//        
//        // 2. 批量處理購物車商品
//        for (CartItem cartItem : cartItems) {
//            createRealOrderItem(orderNo, cartItem.getProNo(), cartItem.getQuantity());
//        }
//        
//        // 3. 清空購物車
//        clearCart(memNo);
//    }
//    
//    
//    // 創建真實訂單項目（從購物車 + 產品表自動組合）
//    private void createRealOrderItem(Integer orderNo, Integer productId, Integer quantity) {
//        orderItemRepository.insertRealOrderItem(orderNo, productId, quantity);
//    }
//    
//    // 從 Redis 獲取購物車
//    private List<CartItem> getCartFromRedis(String userId) {
//        // Redis 購物車邏輯
//        return redisTemplate.opsForList().range("cart:" + userId, 0, -1);
//    }
//    
    
}