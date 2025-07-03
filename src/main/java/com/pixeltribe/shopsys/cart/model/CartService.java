package com.pixeltribe.shopsys.cart.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixeltribe.shopsys.order.model.Order;
import com.pixeltribe.shopsys.order.model.OrderService;

@Service
@Transactional
public class CartService {
	
	@Autowired
	private CartService cartService;  // 注入訂單服務
	
	
	// ============ 加入商品到購物車 ===========//
	public CartDTO addToCart(Integer id, Integer proNo, String proName, 
							Integer proPricer, Integer proNum) {
		
		// 實作，讓程式碼能編譯通過
		CartDTO cart = new CartDTO();
		cart.setId(id);
		cart.setItem(new ArrayList<>());
		
		// 建立新產品項目
		CartDTO.CartItem newItem = new CartDTO.CartItem();
		newItem.setProNo(proNo);
		newItem.setProName(proName);
		newItem.setProPrice(proPricer);
		newItem.setProNum(proNum);
		newItem.calculateTotal();  //計算小計
		
		// 加進去購物車
		cart.getItem().add(newItem);
		
		// 計算總計
		cart.calculateTotals();
		
		return cart;  // 回傳到CartDTO
	}
	
	// ============ 獲取會員購物車 ===========//
	public CartDTO getMemberCart(Integer id) {
		
		// 實作，讓程式碼能編譯通過
		CartDTO cart =new CartDTO();
		cart.setId(id);
		cart.setItem(new ArrayList<>());
		cart.calculateTotals();
		
		return cart;  // 回傳到CartDTO
	}
	
	// ============ 刪掉購物車的產品 ===========//
	
	
	// ============ 更新產品的數量 ===========//
	
	// ============ 清空購物車 ===========//
	public void clearCar(Integer id) {
		
		// 實作，讓程式碼能編譯通過
		System.out.println("清空會員" + id + "的購物車");
	}
	
	}
	
	

