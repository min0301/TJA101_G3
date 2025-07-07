package com.pixeltribe.shopsys.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.cart.model.AdminCartListResponse;
import com.pixeltribe.shopsys.cart.model.CartDTO;
import com.pixeltribe.shopsys.cart.model.CartService;
import com.pixeltribe.shopsys.cart.model.CartStatisticsResponse;

import jakarta.servlet.http.HttpServletRequest;




@RestController
public class CartController {
	
	@Autowired
	private CartService cartService;
	
	// ******** 前台API (會員對購物車的操作) ******** //
	// ==========  將商品加到購物車 ============ //
	@PostMapping("/api/cart/add")
	public ResponseEntity<CartDTO> addToCart(
			@RequestParam Integer proNo,
			@RequestParam Integer proNum,
			HttpServletRequest request) {
		
		
		// 取得會員ID
		Integer memNo = (Integer) request.getAttribute("currentId");
		
		// 呼叫CartService
		CartDTO cart = cartService.addToCart(memNo, proNo, proNum);
		
		return ResponseEntity.ok(cart);   // 配對成功
	}
	
	
	// ==========  查詢購物車 ============ //
	@GetMapping("/api/cart/{memNo}")
	public ResponseEntity<CartDTO> getCart(@PathVariable Integer memNo,
											HttpServletRequest request) {
	
		
		Integer currentMemNo = (Integer) request.getAttribute("currentId");
		if (!memNo.equals(currentMemNo)) {
			// 可以拋出權限例外或直接使用當前會員編號
		};
        
		CartDTO cart = cartService.getMemberCart(memNo);
        return ResponseEntity.ok(cart);
	}
	
	
	// ========== 移除購物車商品 ============ //
	@DeleteMapping("/api/cart/remove/{proNo}")
	public ResponseEntity<CartDTO> removeFromCart(
            @PathVariable Integer proNo,
            HttpServletRequest request) {
		
		Integer memNo = (Integer) request.getAttribute("currentId");
        CartDTO cart = cartService.removeFromCart(memNo, proNo);
        
        return ResponseEntity.ok(cart);
	}
	
	// ========== 更新商品數量 ============ //
	@PutMapping("/api/cart/update/{proNo}")
	public ResponseEntity<CartDTO> updateQuantity(
            @RequestParam Integer proNo,
            @RequestParam Integer proNum,
            HttpServletRequest request) {
		
		Integer memNo = (Integer) request.getAttribute("currentId");
        CartDTO cart = cartService.updateCartItemQuantity(memNo, proNo, proNum);
        
        return ResponseEntity.ok(cart);
	}
	
	
	// ========== 清空購物車 ============ //
	@PostMapping("/api/cart/clear")
	public ResponseEntity<String> clearCart(HttpServletRequest request) {
		
		Integer memNo = (Integer) request.getAttribute("currentId");
		cartService.clearCart(memNo);
		
		return ResponseEntity.ok("購物車已清空");
	}
	
	
	// ******** 後台API (管理員查看數據) ******** //
	// ========== 查詢所有購物車 ============ //
	@GetMapping("/api/admin/cart/all")
	public ResponseEntity<AdminCartListResponse> getAllCarts( 
		@RequestParam(defaultValue = "1") Integer page,
		@RequestParam(defaultValue = "10") Integer size,
		@RequestParam(required = false) Integer memNo) {
		
		AdminCartListResponse response = cartService.getAllCartsForAdmin(page, size, memNo);
        return ResponseEntity.ok(response);
	}
	
	// ========== 購物車統計 ============ //
	@GetMapping("/api/admin/cart/statistics")
    public ResponseEntity<CartStatisticsResponse> getCartStatistics() {
        
        CartStatisticsResponse response = cartService.getCartStatistics();
        return ResponseEntity.ok(response);
    }
	
}