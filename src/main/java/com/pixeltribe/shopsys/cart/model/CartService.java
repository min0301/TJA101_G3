package com.pixeltribe.shopsys.cart.model;

import java.util.ArrayList;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class CartService {
	
	@Autowired
	private CartRepository cartRepository;  // 注入 Redis Repository

	
	
	// ============ 加入商品到購物車 ===========//
	public CartDTO addToCart(Integer memNo, Integer proNo, String proName, 
							Integer proPrice, Integer proNum) {
		
		// 取的現有購物車
		CartDTO cart = cartRepository.getCart(memNo);
		
		// 如果購物車不存在，就創建新的
		if (cart == null) {  
			cart = new CartDTO();
			cart.setMemNo(memNo);
			cart.setItem(new ArrayList<>());
		}
		
		// 檢查產品是否已存在
		CartDTO.CartItem existingItem = null;
		for (CartDTO.CartItem item : cart.getItem()) {
			if (item.getProNo().equals(proNo)) {
				existingItem = item;
				break;
			}
		}
		
		// 商品已存在，增加數量
		if (existingItem != null) {
			existingItem.setProNum(existingItem.getProNum() + proNum);
            existingItem.calculateTotal(); // 重新計算小計
		} else {
			// 建立新商品項目
			CartDTO.CartItem newItem = new CartDTO.CartItem();
            newItem.setProNo(proNo);
            newItem.setProName(proName);
            newItem.setProPrice(proPrice);
            newItem.setProNum(proNum);
            newItem.calculateTotal(); // 計算小計
		}
		
		// 計算總價
		cart.calculateTotals();
		
		// 保存到Redis
		cartRepository.saveCart(memNo, cart);
		
		// 回傳到CartDTO
		return cart;  
	}
	
	
	// ============ 獲取會員購物車 ===========//
	public CartDTO getMemberCart(Integer memNo) {
		
		// 取得購物車
		CartDTO cart = cartRepository.getCart(memNo);
		
		if (cart == null) {
			// 購物車不存在，創建空的購物車
            cart = new CartDTO();
            cart.setMemNo(memNo);
            cart.setItem(new ArrayList<>());
            cart.calculateTotals();
        }	
		
		return cart;  // 回傳到CartDTO
	}
	
	
	// ============ 移除購物車的產品 ===========//
	public CartDTO removeFromCart(Integer memNo, Integer proNo) {
		
		// 取得購物車
		CartDTO cart = cartRepository.getCart(memNo);
        
        if (cart == null) {
            // 購物車不存在，創建空的購物車
            cart = new CartDTO();
            cart.setMemNo(memNo);
            cart.setItem(new ArrayList<>());
            cart.calculateTotals();
            return cart;
        }
        
        // 移除指定產品
        cart.getItem().removeIf(item -> item.getProNo().equals(proNo));
        
        // 重新計算總價
        cart.calculateTotals();
        
        // 存到Redis
        cartRepository.saveCart(memNo, cart);
        
        return cart;  // 回傳到CartDTO
	}
	
	
	// ============ 更新產品的數量 ===========//
	public CartDTO updateCartItemQuantity(Integer memNo, Integer proNo, Integer newQuantity) {
		
		// 取得購物車
		CartDTO cart = cartRepository.getCart(memNo);
		
		// 購物車不存在，創建空的購物車
		if (cart == null) {
		    cart = new CartDTO();
		    cart.setMemNo(memNo);
		    cart.setItem(new ArrayList<>());
		    cart.calculateTotals();
		    return cart;
		}
		
		// 找到指定商品並更新數量
		for (CartDTO.CartItem item : cart.getItem()) {
			if (item.getProNo().equals(proNo)) {
				item.setProNum(newQuantity);
                item.calculateTotal(); // 重新計算小計
                break;
			}
		}
		
		// 重新計算總價		
		cart.calculateTotals();
		
		// 存到Redis
		cartRepository.saveCart(memNo, cart);
		
		return cart;  // 回傳到CartDTO
	}
	
	
	// ============ 清空購物車 ===========//
	public void clearCart(Integer memNo) {
		
		// 直接從 Redis 刪除購物車
        cartRepository.deleteCart(memNo);
        
        System.out.println("已清空您的購物車");
		
	}
	
	}
	
	

